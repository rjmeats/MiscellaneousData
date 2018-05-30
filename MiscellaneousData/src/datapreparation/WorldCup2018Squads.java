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
import java.util.stream.Collectors;

import utils.CSV;
import utils.FileWriter;

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
		
		DataHandler handler = new DataHandler(reader);
		handler.process();
	}	
}

class DataHandler {
	
	List<Team> m_teams;
	List<Player> m_players;
	
	HashMap<String, Integer> m_unrecognisedClubNames;
	HashMap<Club, Integer> m_clubs;
	
	String m_sourceFilePath;
	
	DataHandler(FileReader reader) {
		m_teams = reader.m_teams;
		m_players = reader.m_players;
		
		m_unrecognisedClubNames = new HashMap<>();
		m_clubs = new HashMap<>();
		
		m_sourceFilePath = reader.m_filePath;
	}
	
	void process() {
		
		for(Player p : m_players) {
			if(p.m_club != null) {
				if(m_clubs.containsKey(p.m_club)) {
					int count = m_clubs.get(p.m_club) + 1;
					m_clubs.replace(p.m_club, count);
				}
				else {
					m_clubs.put(p.m_club, 1);
				}
			}
			else {
				if(m_unrecognisedClubNames.containsKey(p.m_clubName)) {
					int count = m_unrecognisedClubNames.get(p.m_clubName) + 1;
					m_unrecognisedClubNames.replace(p.m_clubName, count);
				}
				else {
					m_unrecognisedClubNames.put(p.m_clubName, 1);
				}				
			}
		}
		
		summarise();
		
		dumpCSV();
	}
	
	void summarise() {
		
		System.out.println();
		System.out.println("Found " + m_unrecognisedClubNames.size() + " unrecognised club names:");
		System.out.println();
		m_unrecognisedClubNames.entrySet().stream()
			.sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed().thenComparing(Map.Entry<String, Integer>::getKey))
			.filter(e -> e.getValue() >= 1)
			.forEach(System.out::println);
				
		System.out.println();
		System.out.println("Found " + m_clubs.size() + " clubs");
		System.out.println();

		m_clubs.entrySet().stream()
			.sorted(Comparator.comparing(Map.Entry<Club, Integer>::getValue).reversed().thenComparing(e -> e.getKey().getClubName()))
			.filter(e -> e.getValue() >= 10)
			.forEach(e -> System.out.println(e.getKey().getClubName() + " " + e.getValue()));

		System.out.println();
		System.out.println("Found " + m_players.size() + " players");
		
		System.out.println();
		System.out.println("Players with no club name assigned:");
		m_players.stream().filter(p -> !p.clubNameKnown()).forEach(System.out::println);
		
		System.out.println();
		System.out.println("Teams:");
		//Map<Team, List<Player>> teams = m_players.stream().collect(Collectors.groupingBy(Player::getTeam));
		//for(Map.Entry<Team, List<Player>> entry : teams.entrySet()) {
		//	System.out.println("  " + entry.getKey().getName() + " : " + entry.getValue().size() + " players");
		//}

		//m_teams.stream().sorted(Comparator.comparing(Team::isFinal)).forEach(t -> System.out.println(t.m_teamName.m_name + " : " + t.m_players.size() + " players : " + (t.m_isFinal ? " final " : " to be confirmed")));	
	}
	
	void dumpCSV() {
		String outputFile = m_sourceFilePath.replaceAll(".txt", ".csv").replaceAll("src_data", "output_data");
		if(outputFile.equalsIgnoreCase(m_sourceFilePath)) {
			System.err.println("No output file produced - name clash with existing file");
			return;
		}
		else {
			
			String nl = System.lineSeparator();
			String header = Player.csvHeader();
			String body = m_players.stream().map(p -> p.asCSV()).collect(Collectors.joining(nl));
			
			String csvOutput = header + nl + body;
			FileWriter.writeUTF8File(outputFile, csvOutput);			
		}
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

	String m_filePath;
	
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
		m_positionNameMap.put("Midfielders", Position.Midfielder);
		m_positionNameMap.put("Forwards", Position.Forward);
		m_positionNameMap.put("Strikers", Position.Forward);
		m_positionNameMap.put("Attackers", Position.Forward);
		
		m_clubs = new HashMap<>();
		
		m_filePath = "";
	}
	
	void readFromFile(String filePath) {
		
		m_filePath = filePath;
		
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
	
	void setClubName(String clubName) {
		m_clubName = clubName;
		Club c = Club.getClubFromName(clubName);
		if(c != null) {
			m_club = c;
			//System.out.println("Known club: " + clubName);
		}
		else {
			m_club = c;
			// System.err.println("Unknown club (" + ++unknowns + ") : " + clubName);
		}
	}
	
	boolean clubNameKnown() {
		return m_clubName != null;
	}

	public Team getTeam() {
		return m_team;
	}
	
	public boolean clubInTeamCountry() {
		Club c = m_club == null ? Club.DummyClub : m_club; 
		return m_team.getGeoCountryName().equalsIgnoreCase(c.m_geoCountryName);
	}
	
	public String toString() {
		return "Player: " + m_playerName + " / " + m_team.getName() + " / " + m_position + " / " + m_clubName; 
	}
	
	static String comma = ",";
	
	static String csvHeader() {
		return "Player" + comma + 
				"National Team" + comma +
				"National Team Country" + comma +
				"Group" + comma +
				"Position" + comma +
				"Club Name" + comma +
				"Club Town" + comma +
				"Club Country" + comma +
				"Raw Club Name" + comma +
				"Same Country";
	}
	
	String asCSV() {
		
		Club c = m_club == null ? Club.DummyClub : m_club; 
		return 	CSV.protect(m_playerName) + comma +
				CSV.protect(m_team.getName()) + comma +
				CSV.protect(m_team.getGeoCountryName()) + comma +
				m_group + comma +
				m_position.name() + comma +
				CSV.protect(c.m_clubName) + comma +
				CSV.protect(c.m_town) + comma + 
				CSV.protect(c.m_geoCountryName) + comma +
				CSV.protect(m_clubName) + comma +
				(clubInTeamCountry() ? "Y" : "N");
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

	String getGeoCountryName() {
		return m_teamName.m_geoCountryName;
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
	England("England", "UK"),		// NB Tableau doesn't know England as a country
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
	Goalkeeper, Defender, Midfielder, Forward;
}

enum Club {

	Arsenal("London", "UK"),
	Aston_Villa("Birmingham", "UK"),
	Birmingham_City("UK"),
	Brentford("London", "UK"),
	Brighton("UK"),
	Bristol_City("UK"),
	Burnley("UK"),
	Cardiff_City("UK"),
	Chelsea("London", "UK"),
	Crystal_Palace("London", "UK"),
	Everton("Liverpool", "UK"),
	Huddersfield("UK"),
	Hull_City("UK"),
	Ipswich("UK"),
	Leicester("UK"),
	Leeds_United("UK"),
	Liverpool("UK"),
	Manchester_City("UK"),
	Manchester_United("UK"),
	Millwall("London", "UK"),
	Newcastle_United("Newcastle", "UK"),
	Nottingham_Forest("Nottingham", "UK"),
	QPR("London", "UK"),
	Reading("UK"),
	Southampton("UK"),
	Swansea_City("UK"),
	Stoke_City("Stoke-on-Trent", "UK"),
	Sunderland("UK"),
	Tottenham_Hotspur("London", "UK"),
	Watford("UK"),
	West_Brom("West Bromwich", "UK"),
	West_Ham("London", "UK"),
	Wigan_Athletic("UK"),
	Wolves("Wolverhampton", "UK"),

	Aberdeen("UK"),
	Celtic("Glasgow", "UK"),
	Dundee_United("UK"),
	Rangers("Glasgow", "UK"),

	Alaves("Vitoria-Gasteiz", "Spain"),
	Athletic_Bilbao("Bilbao", "Spain"),
	Atletico_Madrid("Madrid", "Spain"),
	Barcelona("Spain"),
	Celta_Vigo("Vigo", "Spain"),
	Deportivo_La_Coruna("A Coruna", "Spain"), 
	Deportivo_Fabril("A Coruna", "Spain"),
	Eibar("Spain"),
	Espanyol("Barcelona", "Spain"),
	Getafe("Madrid", "Spain"),
	Girona("Spain"),
	Granada("Spain"),
	Las_Palmas("Spain"),						// Tableau knows this as 'Las Palmas de Gran Canaria' 
	Leganes("Madrid", "Spain"),
	Leonesa("Leon", "Spain"),
	Levante("Valencia", "Spain"),
	Numancia("Soria", "Spain"),
	Real_Betis("Seville", "Spain"),
	Real_Madrid("Madrid", "Spain"),
	Real_Sociedad("San Sebastian", "Spain"),
	Sevilla("Seville", "Spain"),
	Valencia("Spain"),
	Villarreal("Spain"),						// Tableau knows this as 'Vila-real' 
	
	Bayer_Leverkusen("Leverkusen", "Germany"),
	Bayern_Munich("Munich", "Germany"),
	Borussia_Dortmund("Dortmund", "Germany"),
	Borussia_Monchengladbach("Monchengladbach", "Germany"),
	Cologne("Germany"),
	Eintracht_Frankfurt("Frankfurt", "Germany"),
	FC_Augsburg("Germany"),
	FC_Nurnberg("Germany"),
	Hamburg_SV("Hamburg", "Germany"),
	Hannover_96("Hannover", "Germany"),
	Hertha_Berlin("Berlin", "Germany"),
	Hoffenheim("Sinsheim", "Germany"),
	Fortuna_Dusseldorf("Dusseldorf", "Germany"),
	Hamburg("Germany"),
	Mainz("Germany"),
	RB_Leipzig("Leipzig", "Germany"),
	Saint_Pauli("Hamburg", "Germany"),
	SC_Freiburg("Freiburg", "Germany"),
	Schalke_04("Gelsenkirchen", "Germany"),
	Stuttgart("Germany"),
	VfL_Bochum("Bochum", "Germany"),
	Werder_Bremen("Bremen", "Germany"),
	Wolfsburg("Germany"),
	
	AC_Milan("Milan", "Italy"),
	Atalanta("Bergamo", "Italy"),
	Bologna("Italy"),
	Cesena("Italy"),
	Crotone("Italy"),
	Genoa("Italy"),
	Fiorentina("Florence", "Italy"),
	Hellas_Verona("Verona", "Italy"),
	Inter_Milan("Milan", "Italy"),
	Palermo("Italy"),
	Juventus("Turin", "Italy"),
	Lazio("Rome", "Italy"),
	Napoli("Naples", "Italy"),
	Roma("Rome", "Italy"),
	Sampdoria("Genoa", "Italy"),
	SPAL("Ferrara", "Italy"),
	Torino("Turin", "Italy"),
	Udinese("Udine", "Italy"),
	
	Amiens("France"),
	Angers("France"),
	Bordeaux("France"),
	Caen("France"),
	Chateauroux("France"),
	Dijon("France"),
	Guingamp("France"),						// Not known by Tableau	Lat/Long = 48.5633 / -3.15
	Lens("France"),
	Lille_OSC("Lille", "France"),
	Lyon("Lyon", "France"),
	Marseille("Marseille", "France"),
	Metz("France"),
	Monaco("Monaco"),
	Montpellier("France"),
	Nantes("France"),
	Nice("France"),
	Paris_St_Germain("Paris", "France"),
	Rennes("France"),
	Toulouse("France"),
	Tours("France"),
	Troyes("France"),
	
	Benfica("Lisbon", "Portugal"),
	Braga("Portugal"),
	Maritimo("Funchal", "Portugal"),
	Porto("Portugal"),
	Sporting_Lisbon("Lisbon", "Portugal"),
	Vitoria_de_Guimaraes("Guimaraes", "Portugal"),
	
	Ado_Den_Haag("The Hague", "Netherlands"),
	Ajax("Amsterdam", "Netherlands"),
	AZ_Alkmaar("Alkmaar", "Netherlands"),
	Feyenoord("Rotterdam", "Netherlands"),
	Heerenveen("Netherlands"),
	PSV_Eindhoven("Eindhoven", "Netherlands"),
	
	Anderlecht("Belgium"),
	Cercle_Brugge("Bruges", "Belgium"),
	Club_Brugge("Bruges", "Belgium"),
	Eupen("Belgium"),
	Genk("Belgium"),
	Gent("Ghent", "Belgium"),
	KSC_Lokeren_Oost_Vlaanderen("Lokeren", "Belgium"),
	Ostende("Belgium"),
	Standard_Liege("Liege", "Belgium"),
	Waasland_Beveren("Waasland-Beveren", "Beveren", "Belgium"),
	
	Red_Bull_Salzburg("Salzburg", "Austria"),
	
	FC_Lausanne_Sport("FC Lausanne-Sport", "Lausanne", "Switzerland"),
	FC_Luzern("Switzerland"),
	Grasshopper_Zurich("Zurich", "Switzerland"),
		
	Aalborg("Denmark"),
	Aarhus("Denmark"),
	Brondby("Copenhagen", "Denmark"),
	FC_Copenhagen("Denmark"),
	FC_Midtjylland("Herning", "Denmark"),
	FC_Nordsjælland("Farum", "Denmark"),
	FC_Roskilde("Denmark"),
	Nordsjaelland("Farum", "Denmark"),
	Randers_FC("Denmark"),
	
	Hammarby("Stockholm", "Sweden"),
	Malmo_FF("Malmo", "Sweden"),
	Ostersunds("Ostersund", "Sweden"),
	
	KuPS("Kuopio", "Finland"),

	Rosenborg("Trondheim", "Norway"),
	Valerenga("Oslo", "Norway"),
	
	Gornik_Zabrze("Zabrze", "Poland"),
	Jagiellonia_Bialystok("Bialystok", "Poland"),
	Lechnia_Gdansk("Gdansk", "Poland"),
	Legia_Warsaw("Warsaw", "Poland"),

	Dunajska_Streda("Slovakia"),				// Not known by Tableau	Lat/Long = 47.994444 / 17.619444
	
	Dinamo_Bucharest("Bucharest", "Romania"),
	
	Lokomotiv_Plovdiv("Plovdiv", "Bulgaria"),
	Ludogorets_Razgrad("Razgrad", "Bulgaria"),
	
	Partizan_Belgrade("Belgrade", "Serbia"),
	Red_Star_Belgrade("Belgrade", "Serbia"),
	
	Dinamo_Zagreb("Zagreb", "Croatia"),
	Hajduk_Split("Split", "Croatia"),
	NK_Lokomotiva("Zagreb", "Croatia"),
	
	AEK_Athens("Athens", "Greece"),
	Atromitos_Athens("Athens", "Greece"),
	Olympiacos("Piraeus", "Greece"),
	PAOK_Salonika("Thessaloniki", "Greece"),
	
	Akhmat_Grozny("Grozny", "Russia"),
	Amkar_Perm("Perm", "Russia"),
	Arsenal_Tula("Tula", "Russia"),
	CSKA_Moscow("Moscow", "Russia"),
	Dynamo_Moscow("Moscow", "Russia"),
	FC_Rostov("Russia"),
	Lokomotiv_Moscow("Moscow", "Russia"),
	Krasnodar("Russia"),
	Rubin_Kazan("Kazan", "Russia"),
	Spartak_Moscow("Moscow", "Russia"),
	Zenit_St_Petersburg("St Petersburg", "Russia"),

	Dynamo_Kyiv("Kiev", "Ukraine"),
	Shakhtar_Donetsk("Donetsk", "Ukraine"),
	
	Alanyaspor("Alanya", "Turkey"),
	Basaksehir("Istanbul", "Turkey"),
	Besiktas("Istanbul", "Turkey"),
	Bursaspor("Bursa", "Turkey"),
	Fenerbahce("Istanbul", "Turkey"),
	Galatasaray("Istanbul", "Turkey"),
	Goztepe("Izmir", "Turkey"),
	Kardemir_Karabukspor("Karabuk", "Turkey"),		// Not known by Tableau	Lat/Long = 41.198611 / 32.626389
	Kasimpasa("Istanbul", "Turkey"),
	Malatyaspor("Malatya", "Turkey"),
	Trabzonspor("Trabzon", "Turkey"),
	
	Hapoel_Beer_Sheva("Hapoel Be'er Sheva", "Beersheba", "Israel"), 
	Maccabi_Haifa("Haifa", "Israel"),
	Maccabi_Tel_Aviv("Tel Aviv", "Israel"),
	
	Banfield("Buenos Aires", "Argentina"),
	Boca_Juniors("Buenos Aires", "Argentina"),
	Independiente("Buenos Aires", "Argentina"),
	River_Plate("Buenos Aires", "Argentina"),
	Velez_Sarsfield("Buenos Aires", "Argentina"),
	
	Corinthians("Sao Paulo", "Brazil"),
	Cruzeiro("Belo Horizonte", "Brazil"),	
	Flamengo("Rio de Janeiro", "Brazil"),
	Gremio("Porto Alegre", "Brazil"),
	Palmeiras("Sao Paulo", "Brazil"),
	Sao_Paulo("Brazil"),
	Vasco_da_Gama("Rio de Janeiro", "Brazil"),
	
	Penarol("Montevideo", "Uruguay"),
	
	Al_Ahli("Al-Ahli", "Jeddah", "Saudi Arabia"),
	Al_Baten("Al-Baten", "Hafar Al-Batin", "Saudi Arabia"),
	Al_Ettifaq("Al-Ettifaq", "Dammam", "Saudi Arabia"),
	Al_Fateh("Al-Fateh", "Al-Ahsa", "Saudi Arabia"),
	Al_Hilal("Al-Hilal", "Riyadh", "Saudi Arabia"),
	Al_Ittihad("Al-Ittihad", "Jeddah", "Saudi Arabia"),
	Al_Nassr("Al-Nassr", "Riyadh", "Saudi Arabia"),
	Al_Raed("Al-Raed", "Buraydah", "Saudi Arabia"),
	Al_Shabab("Al-Shabab", "Riyadh", "Saudi Arabia"),
	Al_Taawoun("Al-Taawoun", "Buraydah", "Saudi Arabia"),
	
	Al_Ahly("Al-Ahly", "Cairo", "Egypt"),
	Al_Masry("Al-Masry", "Port Said", "Egypt"),
	Ismaily("Ismailia", "Egypt"),
	Zamalek("Giza", "Egypt"),
	
	Al_Ain("Al-Ain", "Abu Dhabi", "UAE"),
	Al_Jazira("Al-Jazira", "Abu Dhabi", "UAE"),
	Al_Nasr("Al-Nasr", "Dubai", "UAE"),
	
	Al_Gharafa("Al-Gharafa", "Doha", "Qatar"),
	Al_Sadd("Al-Sadd", "Doha", "Qatar"),
	
	Esteghlal("Tehran", "Iran"),
	Padideh("Mashhad", "Iran"),
	Persepolis("Tehran", "Iran"),
	Saipa("Tehran", "Iran"),
	Zob_Ahan("Isfahan", "Iran"),
	
	Cerezo_Osaka("Osaka", "Japan"),
	Gamba_Osaka("Osaka", "Japan"),
	Kashima_Antlers("Kashima", "Japan"),				// Ambiguous in Tableau Lat/Long = 35.965639 / 140.644833
	Kashiwa_Reysol("Kashiwa", "Japan"),
	Kawasaki_Frontale("Kawasaki", "Japan"),				// Ambiguous in Tableau Lat/Long = 35.516667 / 139.7
	Sagan_Tosu("Tosu", "Japan"),
	Sanfrecce_Hiroshima("Hiroshima", "Japan"),
	Tokyo_FC("Japan"),
	Urawa_Reds("Saitama", "Japan"),
	Vissel_Kobe("Kobe", "Japan"),
	Urawa_Red_Diamonds("Saitama", "Japan"),
	Yokohama_F_Marinos("Yokohama", "Japan"),
	
	Asan_Mugunghwa("Asan", "South Korea"),
	Daegu_FC("South Korea"),
	FC_Seoul("South Korea"),
	Incheon_Utd("South Korea"),
	Jeju_Utd("South Korea"),
	Jeonbuk_Hyundai("Jeonju", "South Korea"),
	Sangju_Sangmu("Sangju", "South Korea"),
	Seongnam_FC("Seongnam", "South Korea"),				// Tableau knows this as 'Songnam' ('Sŏngnam' actually when looking up)
	Suwon_Samsung_BlueWings("Suwon", "South Korea"),
	Ulsan_Hyundai("Ulsan", "South Korea"),
	
	Beijing_Guoan("Beijing", "China"),
	Changchun_Yatai("Changchun", "China"),
	Dalian_Yifang("Dalian", "China"),
	Guangzhou_Evergrande("Guangzhou", "China"),
	Guangzhou_RF("Guangzhou R&F", "Guangzhou", "China"),
	Hebei_Fortune("Langfang", "China"),
	Shanghai_Shenhua("Shanghai", "China"),
	Tianjin_Quanjian("Tianjin", "China"),
	Tianjin_Teda("Tianjin", "China"),
	
	Ittihad_Tanger("Tangier", "Morocco"),
	Raja_Casablanca("Casablanca", "Morocco"),
	Renaissance_Berkane("Berkane", "Morocco"),
	
	Club_Africain("Tunis", "Tunisia"),
	CS_Sfaxien("Sfax", "Tunisia"),
	ES_Sahel("Sousse", "Tunisia"),
	ES_Tunis("Tunis", "Tunisia"),
	Etoile_du_Sahel("Sousse", "Tunisia"),
	
	Enyimba("Aba", "Nigeria"),
	Kano_Pillars("Kano", "Nigeria"),
	Plateau_United("Jos", "Nigeria"),
	
	Chippa_United("Port Elizabeth", "South Africa"),
	
	Horoya_AC("Conakry", "Guinea"),
	
	Atlas("Guadalajara", "Mexico"),
	America("Mexico City", "Mexico"),
	Cafetaleros_de_Tapachula("Tapachula", "Mexico"),
	Cruz_Azul("Mexico City", "Mexico"),
	Guadalajara("Mexico"),
	Lobos_BUAP("Puebla", "Mexico"),					// Ambiguous in Tableau Lat/Long = 19.033333 / -98.183333
	Mineros_Zacatecas("Zacatecas", "Mexico"),
	Monterrey("Mexico"),
	Morelia("Mexico"),
	Pachuca("Pachuca", "Mexico"),				
	Puebla("Mexico"),
	Pumas("Mexico City", "Mexico"),
	Tigres("Monterrey", "Mexico"),
	Toluca("Mexico"),
	Veracruz("Mexico"),								// Ambiguous in Tableau Lat/Long = 19.190278 / -96.153333

	Alajuelense("Alajuela", "Costa Rica"),
	Deportivo_Saprissa("San Jose", "Costa Rica"),
	Herediano("Heredia", "Costa Rica"),
	Santos_de_Guapiles("Guapiles", "Costa Rica"),
	
	Bucaramanga("Colombia"),
	Deportivo_Cali("Cali", "Colombia"),
	Junior("Barranquilla", "Colombia"),
	Once_Caldas("Manizales", "Colombia"),
	Patriotas("Tunja", "Colombia"),
	Rionegro_Aguilas("Rionegro", "Colombia"),
	Santa_Fe("Bogota", "Colombia"),
	
	Olimpia("Asuncion", "Paraguay"),
	
	Alianza_Lima("Lima", "Peru"),
	Universitario("Lima", "Peru"),
	Melgar("Arequipa", "Peru"),
	Sports_Boys("Callao", "Peru"),
	Union_Comercio("Nueva Cajamarca", "Peru"),
	Universidad_San_Martin("Lima", "Peru"),
	UTC("Cajamarca", "Peru"),

	Huachipato("Talcahuano", "Chile"),
	Universidad_de_Chile("Santiago", "Chile"),

	Alianza("Panama City", "Panama"),
	Chorrillo("Panama City", "Panama"),
	Plaza_Amador("Panama City", "Panama"),
	Tauro("Panama City", "Panama"),
	
	Municipal("Guatemala City", "Guatemala"),
	
	Columbus_Crew("Columbus", "USA"),				// Ambiguous in Tableau Lat/Long = 39.983333 / -82.983333
	Houston_Dynamo("Houston", "USA"),				// Ambiguous in Tableau Lat/Long = 29.762778 / -95.383056
	LA_Galaxy("Los Angeles", "USA"),
	Los_Angeles_FC("USA"),
	Minnesota_United("Minneapolis", "USA"),			// Ambiguous in Tableau Lat/Long = 44.983333 / -93.266667
	New_York_City("New York", "USA"),
	New_York_Red_Bulls("New York", "USA"),
	Orlando_City("USA"),							// Ambiguous in Tableau Lat/Long = 28.54 / -81.38
	Portland_Timbers("Portland", "USA"),			// Ambiguous in Tableau Lat/Long = 45.52 / -122.681944
	San_Francisco("San Francisco", "USA"),
	San_Jose_Earthquakes("San Jose", "USA"),		// Ambiguous in Tableau Lat/Long = 37.333333 / -121.9
	Seattle_Sounders("Seattle", "USA"),
	
	Vancouver_Whitecaps("Vancouver", "Canada"),
	
	Melbourne_City("Australia"),
	Melbourne_Victory("Melbourne", "Australia"),
	Newcastle_Jets("Newcastle", "Australia"),
	Sydney_FC("Australia"),
	Western_Sydney("Sydney", "Australia"),
	
	DummyClub("DummyClub", "DummyTown", "DummyCountry");
	;
	
	String m_clubName;
	String m_possibleShortName;
	String m_town;
	String m_geoCountryName;
	
	Club(String country) {
		m_clubName = name().replaceAll("_",  " ");
		m_town = m_clubName.replaceAll(" City", "").replaceAll(" United", "").replaceAll(" Utd", "").replaceAll(" Athletic", "");
		if(m_town.startsWith("FC ")) {
			m_town = m_town.substring(3);
		}
		if(m_town.endsWith(" FC")) {
			m_town = m_town.substring(0, m_town.length()-3);
		}
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
	
	String getClubName() {
		return m_clubName;
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
		addMapping("El Ettifaq", Al_Ettifaq);				// Typo in BBC page ?
		addMapping("Aalborgk", Aalborg);					// Typo in BBC page ?
		addMapping("Krasnador", Krasnodar);					// Typo in BBC page ?
		addMapping("tottenham Hotspur", Tottenham_Hotspur); // Typo in BBC page ?
		addMapping("Granadan", Granada);					// Typo in BBC page ?
		addMapping("Patriots", Patriotas);					// Typo in BBC page ?
		addMapping("Suwon Samsung Blue Wings", Suwon_Samsung_BlueWings);	// Typo in BBC page ?
		addMapping("Tottenham", Tottenham_Hotspur);		
		addMapping("Sporting", Sporting_Lisbon);
		addMapping("Paris Saint-Germain", Paris_St_Germain);
		addMapping("Paris St-Germain", Paris_St_Germain);
		addMapping("Dortmund", Borussia_Dortmund);
		addMapping("Milan", AC_Milan);
		addMapping("AS Roma", Roma);
		addMapping("AS Monaco", Monaco);
		addMapping("Schalke", Schalke_04);
		addMapping("African Club", Club_Africain);
		addMapping("C.S. Herediano", Herediano);
		addMapping("Veracruz-MEX", Veracruz);
		addMapping("Junior-Colombia", Junior);
		addMapping("Ajax Amsterdam", Ajax);
		addMapping("Al-Ittifaq", Al_Ettifaq);
		addMapping("Alliance", Alianza);
		addMapping("Alsaad", Al_Sadd);
		addMapping("KSC Lokeren Oost-Vlaanderen", KSC_Lokeren_Oost_Vlaanderen);
		addMapping("LOSC", Lille_OSC);
		addMapping("Yokohama F. Marinos", Yokohama_F_Marinos);
		addMapping("CD Huachipato", Huachipato);
		addMapping("Deportivo de La Coruna", Deportivo_La_Coruna);
		addMapping("FSV Mainz", Mainz);
		addMapping("Flamengo-Brazil", Flamengo);
		addMapping("Liga Deportiva Alajuelense", Alajuelense);
		addMapping("Lobos BUAP-Mexico", Lobos_BUAP);
		addMapping("Lokomotiv", Lokomotiv_Moscow);
		addMapping("Municipal CSD", Municipal);
		addMapping("Puebla-Mexico", Puebla);
		addMapping("Santos de Guapiles FC", Santos_de_Guapiles);
		addMapping("Sporting CP", Sporting_Lisbon);
		addMapping("Standard de Liege", Standard_Liege);
		addMapping("Velez Sarsfield-Argentina", Velez_Sarsfield);
		addMapping("Veracruz-Mexico", Veracruz);
		addMapping("Stoke", Stoke_City);
		
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

		// And index again with '.'s removed.
		if(name.indexOf(".") != -1) {
			addMapping(name.replaceAll("\\.",  ""), c);
		}
	}
	
	static Club getClubFromName(String name) {
		return s_nameMappings.get(name.replaceAll("\\.", ""));
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