package datapreparation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.regex.*;
//import java.util.stream.Collectors;

public class WorldCup2018Squads {

	public static void main(String args[]) {

		if (args.length == 0) {
			System.err.println("No file path provided");
			return;
		}

		String filePath = args[0];
		System.out.println("Reading from file: " + args[0]);

		Club.setUpMappings();

		FileReader reader = new FileReader();
		reader.readFromFile(filePath);		
	}	
}

class FileReader {

	String m_currentGroup;
	Team m_currentTeam;
	
	List<Team> m_teams;
	List<Player> m_players;
	
	HashMap<String, TeamName> m_teamNameMap;
	HashMap<String, Position> m_positionNameMap;
	HashMap<String, Integer> m_clubs;	

	FileReader() {
		m_teams = new ArrayList<>();
		m_players = new ArrayList<>();		
		m_teamNameMap = new HashMap<>();
		for(TeamName tn : TeamName.values()) {
			m_teamNameMap.put(tn.m_name, tn);
		}
		
		m_positionNameMap = new HashMap<>();
		m_positionNameMap.put("Goalkeepers", Position.Goalkeeper);
		m_positionNameMap.put("Defenders", Position.Defender);
		m_positionNameMap.put("Midfielders", Position.Midfield);
		m_positionNameMap.put("Forwards", Position.Forward);
		m_positionNameMap.put("Strikers", Position.Forward);
		m_positionNameMap.put("Attackers", Position.Forward);
		
		m_clubs = new HashMap<>();
		
	}
	
	void readFromFile(String filePath) {
		
		File f = new File(filePath);
		
		// Check the file is readable 
		if(!(f.exists() && f.isFile() && f.canRead())) { 
			System.err.println("Unable to read file " + filePath); 
		} 
		else {

			m_currentGroup = null;
			m_currentTeam = null; 
			
			char BOM = '\uFEFF'; 
			try (Scanner sc = new Scanner(f)) { 		// NB UTF-8
				while(sc.hasNext()) {
					String line = sc.nextLine().trim();
					if(line.length() == 0) continue;
					if(line.charAt(0) == BOM) {
						line = line.substring(1);
					}
					processLine(line);
				}
			} catch (FileNotFoundException e) {
				System.err.println("Failure reading file " + filePath + " : " + e.getMessage());
			}
		}
		
		System.out.println();
		System.out.println("Found " + m_clubs.size() + " clubs");
		System.out.println();

		m_clubs.entrySet().stream().sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()).forEachOrdered(System.out::println);
//		for(Map.Entry<String, Integer> clubInfo : m_clubs.entrySet()) {
//			System.out.println("Club: " + clubInfo.getKey() + " : " + clubInfo.getValue());
//		}

		System.out.println();
		System.out.println("Found " + m_players.size() + " players");
		
		System.out.println();
		System.out.println("Players with no club assigned:");
		m_players.stream().filter(p -> !p.clubNameKnown()).forEach(System.out::println);
		
		System.out.println();
		System.out.println("Teams:");
		//Map<Team, List<Player>> teams = m_players.stream().collect(Collectors.groupingBy(Player::getTeam));
		//for(Map.Entry<Team, List<Player>> entry : teams.entrySet()) {
		//	System.out.println("  " + entry.getKey().getName() + " : " + entry.getValue().size() + " players");
		//}
		m_teams.stream().sorted(Comparator.comparing(Team::isFinal)).forEach(t -> System.out.println(t.m_teamName.m_name + " : " + t.m_players.size() + " players : " + (t.m_isFinal ? " final " : " to be confirmed")));
	}
	
	private void processLine(String line) {
		//System.out.println("Read line (" + line.length() + ") : " + line);
		
		if(line.startsWith("From http")) {
			// Line added manually to record where the data came from - ignore it
		}
		else if(processGroupLine(line)) {
			;
		}
		else if(processCountryLine(line)) {
			;
		}
		else if(processPositionLine(line)) {
			;
		}
		else if(line.startsWith("Yet to be named")) {
			System.err.println("Warning: No players named yet for " + m_currentTeam.getName());
			this.m_currentTeam.setIsFinal(false);
		}
		else {
			System.err.println("Warning: Line not recognised: " + line);
		}
	}

	// Group B
	static Pattern s_groupLinePattern = Pattern.compile("Group ([A-H])"); 
	boolean processGroupLine(String line) {
		Matcher m = s_groupLinePattern.matcher(line);
		if(m.matches()) {
			m_currentGroup = m.group(1);
			// System.out.println("Found group " + m_currentGroup);
		}
		
		return m.matches();
	}

	// Egypt (final 23 to be confirmed)
	boolean processCountryLine(String line) {
		String adjustedLine = line.replace("(final 23 to be confirmed)", "").trim();
		TeamName tn = m_teamNameMap.get(adjustedLine);
		if(tn != null) {
			Team t = new Team(tn);
			t.setGroup(m_currentGroup);
			t.setIsFinal(line.indexOf("to be confirmed") == -1);
			m_currentTeam = t;
			m_teams.add(t);
			// System.out.println("Found team " + m_currentTeam);
		}
		return tn != null;
	}
	
	// Goalkeepers: Alisson (Roma), Ederson (Manchester City), Cassio (Corinthians).
	static Pattern s_positionLinePattern = Pattern.compile("(.*):\\s+(.*?)\\.?"); 
	boolean processPositionLine(String line) {
		boolean matched = false;
		Matcher m = s_positionLinePattern.matcher(line);
		if(m.matches()) {
			Position pos = m_positionNameMap.get(m.group(1));
			if(pos != null) {
				List<Player> playersWithNoClubName = new ArrayList<>();
				// Split on ")," or ");" or just ")" or ", " or "; "
				String playerStrings[] = m.group(2).split("\\)\\s*,|\\)\\s*;|\\)|,\\s+|;\\s+");
				boolean badPlayerFound = false;
				for(String playerString : playerStrings) {
					playerString = playerString.trim();
					if(playerString.indexOf("(") != -1 && playerString.indexOf(")") == -1) {
						playerString += ")";
					}
					Player player = processPlayerString(pos, playerString);
					if(player == null) {
						badPlayerFound = true;
						System.err.println("Failed to process player string: " + playerString);
					}
					else {
						m_players.add(player);
						m_currentTeam.addPlayer(player);
						if(!player.clubNameKnown()) {
							playersWithNoClubName.add(player);
						}
						
						if(player.clubNameKnown() && playersWithNoClubName.size() > 0) {
							playersWithNoClubName.stream().forEach(p -> p.setClubName(player.m_clubName));
							playersWithNoClubName.clear();
						}
					}
				}
				matched = !badPlayerFound;
				if(!matched) System.err.println("Problem processing : " + line);
				if(playersWithNoClubName.size() > 0) {
					System.err.println("Players with no club for: " + line);
				}
			}
		}
		
		return matched;
	}
	
	// Carlos Bacca (Villarreal)
	static Pattern s_playerPattern = Pattern.compile("(.+?)(?:\\s+\\((.*)\\))?");
	Player processPlayerString(Position position, String playerString) {
		Player player = null;
		Matcher m = s_playerPattern.matcher(playerString);
		if(m.matches()) {
			String playerName = m.group(1);
			String clubName = m.group(2);			
			player = new Player(playerName, m_currentTeam, m_currentGroup, position);
//			System.out.println("Player : " + m.group(1) + ", club : " + clubName + " -  from " + playerString);
			
			// If the second group is null, no team specified, expect to resolve from subsequent teams identified as 'both ...' or 'all ...'
			
			if(clubName != null) {
				if(clubName.startsWith("all ")) {
					clubName = clubName.substring(4);
				}
				if(clubName.startsWith("both ")) {
					clubName = clubName.substring(5);
				}

				player.setClubName(clubName);
				if(m_clubs.containsKey(clubName)) {
					int count = m_clubs.get(clubName) + 1;
					m_clubs.replace(clubName, count);
				}
				else
				{
					m_clubs.put(clubName, 1);
				}
			}
		}
		else {
			// System.err.println("Not processed: " + playerString);			
		}
			
		return player;
	}
}

class Player {
	
	String m_playerName;
	Team m_team;
	String m_group;
	Position m_position;
	String m_clubName;
	Club m_club;
	
	Player(String playerName, Team team, String group, Position position) {
		m_playerName = playerName;
		m_team = team;
		m_group = group;
		m_position = position;
		m_clubName = null;
	}
	
static int unknowns = 0;	
	void setClubName(String clubName) {
		m_clubName = clubName;
		Club c = Club.getClubFromName(clubName);
		if(c != null) {
			m_club = c;
			//System.out.println("Known club: " + clubName);
		}
		else {
			System.err.println("Unknown club (" + ++unknowns + ") : " + clubName);
		}
	}
	
	boolean clubNameKnown() {
		return m_clubName != null;
	}

	public Team getTeam() {
		return m_team;
	}
	
	public String toString() {
		return "Player: " + m_playerName + " / " + m_team.getName() + " / " + m_position + " / " + m_clubName; 
	}
}

class Team {

	TeamName m_teamName;
	String m_group;
	List<Player> m_players;
	boolean m_isFinal;
	
	Team(TeamName teamName) {
		m_teamName = teamName;
		m_group = null;
		m_players = new ArrayList<>();
		m_isFinal = false;
	}
	
	void setGroup(String group) {
		m_group = group;
	}
	
	void setIsFinal(boolean isFinal) {
		m_isFinal = isFinal;
	}
	
	void addPlayer(Player player) {
		m_players.add(player);
	}
	
	String getName() {
		return m_teamName.m_name;
	}

	boolean isFinal() {
		return m_isFinal;
	}

	int playerCount() {
		return m_players.size();
	}
}

enum TeamName {
	
	Russia,
	Saudi_Arabia,
	Egypt,
	Uruguay,
	Portugal,
	Spain,
	Morocco,
	Iran,
	France,
	Australia,
	Peru,
	Denmark,
	Argentina,
	Iceland,
	Croatia,
	Nigeria,
	Brazil,
	Switzerland,
	Costa_Rica,
	Serbia,
	Germany,
	Mexico,
	Sweden,
	South_Korea,
	Belgium,
	Panama,
	Tunisia,
	England("England", "United Kingdom"),
	Poland,
	Senegal,
	Colombia,
	Japan;

	String m_name;
	String m_geoCountryName;
	
	TeamName() {
		m_name = name().replaceAll("_",  " ");
		m_geoCountryName = m_name;
	}
	
	TeamName(String name, String geoCountryName) {
		m_name = name;
		m_geoCountryName = geoCountryName;
	}
	
	public String toString() {
		return m_name;
	}	
}

enum Position {
	Goalkeeper, Defender, Midfield, Forward;
}

enum Club {

	Arsenal("London", "United Kingdom"),
	Aston_Villa("Birmingham", "United Kingdom"),
	Birmingham_City("United Kingdom"),
	Brentford("London", "United Kingdom"),
	Brighton("United Kingdom"),
	Bristol_City("United Kingdom"),
	Burnley("United Kingdom"),
	Cardiff_City("United Kingdom"),
	Chelsea("London", "United Kingdom"),
	Crystal_Palace("London", "United Kingdom"),
	Everton("Liverpool", "United Kingdom"),
	Huddersfield("United Kingdom"),
	Hull_City("United Kingdom"),
	Ipswich("United Kingdom"),
	Leicester("United Kingdom"),
	Leeds_United("United Kingdom"),
	Liverpool("United Kingdom"),
	Manchester_City("United Kingdom"),
	Manchester_United("United Kingdom"),
	Millwall("London", "United Kingdom"),
	Newcastle_United("Newcastle", "United Kingdom"),
	Nottingham_Forest("Nottingham", "United Kingdom"),
	QPR("London", "United Kingdom"),
	Reading("United Kingdom"),
	Southampton("United Kingdom"),
	Swansea_City("United Kingdom"),
	Stoke_City("United Kingdom"),
	Sunderland("United Kingdom"),
	Tottenham_Hotspur("Spurs", "London", "United Kingdom"),
	Watford("United Kingdom"),
	West_Brom("West Bromwich", "United Kingdom"),
	West_Ham("London", "United Kingdom"),
	Wigan_Athletic("United Kingdom"),
	Wolves("Wolverhampton", "United Kingdom"),

	Aberdeen("United Kingdom"),
	Celtic("Glasgow", "United Kingdom"),
	Dundee_United("United Kingdom"),
	Rangers("Glasgow", "United Kingdom"),

	Atletico_Madrid("Madrid", "Spain"),
	Barcelona("Spain"),
	Celta_Vigo("Vigo", "Spain"),
	Eibar("Spain"),
	Espanyol("Barcelona", "Spain"),
	Getafe("Spain"),
	Girona("Spain"),
	Leonesa("Leon", "Spain"),
	Levante("Valencia", "Spain"),
	Real_Madrid("Madrid", "Spain"),
	Sevilla("Seville", "Spain"),
	Valencia("Spain"),
	Villarreal("Spain"),
	
	Augsburg("Germany"),
	Bayer_Leverkusen("Leverkusen", "Germany"),
	Bayern_Munich("Munich", "Germany"),
	Borussia_Dortmund("Dortmund", "Germany"),
	Borussia_Monchengladbach("Monchengladbach", "Germany"),
	Eintracht_Frankfurt("Frankfurt", "Germany"),
	Hertha_Berlin("Berlin", "Germany"),
	Fortuna_Dusseldorf("Dusseldorf", "Germany"),
	Hamburg("Germany"),
	Mainz("Germany"),
	Schalke_04("Gelsenkirchen", "Germany"),
	Stuttgart("Germany"),
	Werder_Bremen("Bremen", "Germany"),
	Wolfsburg("Germany"),
	
	AC_Milan("Milan", "Italy"),
	Bologna("Italy"),
	Genoa("Italy"),
	Fiorentina("Florence", "Italy"),
	Inter_Milan("Milan", "Italy"),
	Juventus("Turin", "Italy"),
	Lazio("Rome", "Italy"),
	Napoli("Naples", "Italy"),
	Roma("Rome", "Italy"),
	Sampdoria("Genoa", "Italy"),
	Torino("Turin", "Italy"),
	Udinese("Udine", "Italy"),
	
	Amiens("France"),
	Bordeaux("France"),
	Caen("France"),
	Chateauroux("France"),
	Dijon("France"),
	Lens("France"),
	Lyon("Lyons", "France"),
	Marseille("Marseilles", "France"),
	Metz("France"),
	Monaco("Monaco"),
	Montpellier("France"),
	Nantes("France"),
	Nice("France"),
	Paris_St_Germain("Paris", "France"),
	Rennes("France"),
	
	Benfica("Lisbon", "Portugal"),
	Porto("Portugal"),
	Sporting_Lisbon("Lisbon", "Portugal"),
	
	Ajax("Amsterdam", "Netherlands"),
	Feyenoord("Rotterdam", "Netherlands"),
	PSV_Eindhoven("Eindhoven", "Netherlands"),
	
	Anderlecht("Belgium"),
	Club_Brugge("Bruges", "Belgium"),
		
	Red_Bull_Salzburg("Salzburg", "Austria"),
	
	Brondby("Denmark"),
	FC_Copenhagen("Copenhagen", "Denmark"),
	
	Legia_Warsaw("Warsaw", "Poland"),
	
	Akhmat_Grozny("Grozny", "Russia"),
	Arsenal_Tula("Tula", "Russia"),
	CSKA_Moscow("Moscow", "Russia"),
	Dynamo_Moscow("Moscow", "Russia"),
	Lokomotiv_Moscow("Moscow", "Russia"),
	Krasnodar("Russia"),
	Rubin_Kazan("Kazan", "Russia"),
	Spartak_Moscow("Moscow", "Russia"),
	Zenit_St_Petersburg("St Petersburg", "Russia"),

	Shakhtar_Donetsk("Donetsk", "Ukraine"),
	
	Atromitos_Athens("Athens", "Greece"),
	
	Besiktas("Istanbul", "Turkey"),
	Bursaspor("Bursa", "Turkey"),
	Fenerbahce("Istanbul", "Turkey"),
	Galatasaray("Istanbul", "Turkey"),
	Kasimpasa("Istanbul", "Turkey"),
	
	KuPS("Kuopio", "Finland"),
	
	Boca_Juniors("Buenos Aires", "Argentina"),
	Independiente("Buenos Aires", "Argentina"),
	River_Plate("Buenos Aires", "Argentina"),
	
	Corinthians("Sao Paulo", "Brazil"),
	Cruzeiro("Belo Horizonte", "Brazil"),
	Flamengo("Rio de Janeiro", "Brazil"),
	Palmeiras("Sao Paulo", "Brazil"),
	
	Penarol("Montevideo", "Uruguay"),
	
	Al_Ahli("Al-Ahli", "Jeddah", "Saudi Arabia"),
	Al_Ettifaq("Al-Ettifaq", "Dammam", "Saudi Arabia"),
	Al_Fateh("Al-Fateh", "Al-Hasa", "Saudi Arabia"),
	Al_Hilal("Al-Hilal", "Riyadh", "Saudi Arabia"),
	Al_Ittihad("Al-Ittihad", "Jeddah", "Saudi Arabia"),
	Al_Nassr("Al-Nassr", "Riyadh", "Saudi Arabia"),
	Al_Raed("Al-Raed", "Buraydah", "Saudi Arabia"),
	Al_Shabab("Al-Shabab", "Riyadh", "Saudi Arabia"),
	Al_Taawoun("Al-Taawoun", "Buraydah", "Saudi Arabia"),
	
	Al_Ahly("Cairo", "Egypt"),
	Al_Masry("Port Said", "Egypt"),
	Ismaily("Ismailia", "Egypt"),
	Zamalek("Giza", "Egypt"),
	
	Al_Jazira("Abu Dhabi", "UAE"),
	
	Esteghlal("Tehran", "Iran"),
	
	Cerezo_Osaka("Osaka", "Japan"),
	Gamba_Osaka("Osaka", "Japan"),
	Kashima_Antlers("Kashima", "Japan"),
	Kashiwa_Reysol("Kashiwa", "Japan"),
	Kawasaki_Frontale("Kawasaki", "Japan"),
	Sanfrecce_Hiroshima("Hiroshima", "Japan"),
	Urawa_Reds("Saitama", "Japan"),
	
	Jeonbuk_Hyundai("Jeonju", "South Korea"),

	Raja_Casablanca("Casablanca", "Morocco"),
	
	Plateau_United("Jos", "Nigeria"),
	
	Chippa_United("Port Elizabeth", "South Africa"),
	
	Monterrey("Mexico"),
	Pachuca("Hidalgo", "Mexico"),
	
	Deportivo_Saprissa("San Jose", "Costa Rica"),
	
	Junior("Barranquilla", "Columbia"),
	
	Los_Angeles_FC("Los Angeles", "USA"),
	Orlando_City("USA"),
	
	Changchun_Yatai("Changchun", "China"),
	
	;
	
	String m_clubName;
	String m_possibleShortName;
	String m_town;
	String m_geoCountryName;
	
	Club(String country) {
		m_clubName = name().replaceAll("_",  " ");
		m_town = m_clubName.replaceAll(" City", "").replaceAll(" United", "").replaceAll(" Athletic", "");
		m_possibleShortName = m_town;
		m_geoCountryName = country;		
	}

	Club(String town, String country) {
		m_clubName = name().replaceAll("_",  " ");
		m_town = town;
		m_possibleShortName = "";
		m_geoCountryName = country;				
	}
	
	Club(String clubName, String town, String country) {
		m_clubName = clubName;
		m_town = town;
		m_possibleShortName = "";
		m_geoCountryName = country;				
	}
	
	private static HashMap<String, Club> s_nameMappings = new HashMap<>();
	private static Set<String> s_conflictNames = new HashSet<>();
	
	static void setUpMappings() {
		s_nameMappings = new HashMap<>();
		s_conflictNames = new HashSet<>();
		for(Club c : Club.values()) {
			addMapping(c.m_clubName, c);
			addMapping(c.name(), c);
			if(c.m_possibleShortName.length() > 0) {
				addMapping(c.m_possibleShortName, c);				
			}
			
			// Add a version with hyphens converted to a space
			if(c.m_clubName.indexOf("-") != -1) {
				addMapping(c.m_clubName.replaceAll("-",  " "), c);
			}
			
			// And with full-stops removed
			if(c.m_clubName.indexOf(".") != -1) {
				addMapping(c.m_clubName.replaceAll(".",  ""), c);
			}
		}
		
		// Add some additional mappings
		addMapping("El Ettifaq", Al_Ettifaq);		// Typo in BBC page ?
		addMapping("Tottenham", Tottenham_Hotspur);		
		addMapping("Sporting", Sporting_Lisbon);
		addMapping("Paris Saint-Germain", Paris_St_Germain);
		addMapping("Paris St-Germain", Paris_St_Germain);
		addMapping("Dortmund", Borussia_Dortmund);
		addMapping("Milan", AC_Milan);
		addMapping("AS Roma", Roma);
		addMapping("Schalke", Schalke_04);
		
		// Remove conflicts
		s_conflictNames.stream().forEach(n -> s_nameMappings.remove(n));
	}
	
	static void addMapping(String name, Club c) {
		if(s_nameMappings.containsKey(name)) {
			Club c2 = s_nameMappings.get(name);
			if(c2 == c) {
				// System.err.println("Repeat club mapping : " + name + " : " + c.name());
			}
			else {
				System.err.println("Conflicting club mapping : " + c.name() + " : " + c2.name() + " : for name '" + name + "'");
				s_conflictNames.add(name);
			}
		}
		else {
			s_nameMappings.put(name, c);
		}
	}
	
	static Club getClubFromName(String name) {
		return s_nameMappings.get(name);
	}
}

// Club name tidy
// Spelling/puntuation/abbreviations/case diffs
// all/both prefixes
// Placeholders ? CLub Africain = African Club
// America = Club America in Mexico
// Milan == AC Milan
// + need to link to place and city
// CLubs with same name but in different countries ?
// Approaching 400 clubs - any benefit in having 'other' ?

// http://www.fifa.com/worldcup/teams/index.html