package datapreparation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
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

		FootballClub.setUpMappings();

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
	HashMap<FootballClub, Integer> m_clubs;
	
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
					//System.err.println("Adding unrec club : " + p.m_clubName);
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
			.sorted(Comparator.comparing(Map.Entry<FootballClub, Integer>::getValue).reversed().thenComparing(e -> e.getKey().getClubName()))
			.filter(e -> e.getValue() >= 10)
			.forEach(e -> System.out.println(e.getKey().getClubName() + " " + e.getValue()));

		System.out.println();
		System.out.println("Found " + m_players.size() + " players");
		
		System.out.println();
		System.out.println("Players with no club name assigned:");
		m_players.stream().filter(p -> !p.clubNameKnown()).forEach(System.out::println);
		
		System.out.println();
		System.out.println("Teams:");
		
		Map<Team, List<Player>> teams = m_players.stream().collect(Collectors.groupingBy(Player::getTeam));
		for(Map.Entry<Team, List<Player>> entry : teams.entrySet()) {
			System.out.println("  " + entry.getKey().getName() + " : " + entry.getValue().size() + " players");
		}

		m_teams.stream().sorted(Comparator.comparing(Team::isFinal)).forEach(t -> System.out.println(t.m_teamName.m_name + " : " + t.m_players.size() + " players : " + (t.m_isFinal ? " final " : " to be confirmed")));	
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
		String adjustedLine = line.replace("(final 23 to be confirmed)", "").replace("(final 23 to be named)", "").trim();
		TeamName tn = m_teamNameMap.get(adjustedLine);
		if(tn != null) {
			Team t = new Team(tn);
			t.setGroup(m_currentGroup);
			t.setIsFinal(line.indexOf("to be ") == -1);
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
		
		// Bodges to handle case of commas not being used as expected
		line = line.replaceAll("Torino, Italy", "Torino");
		line = line.replaceAll("Tarek Hamed, \\(Zamalek\\),", "Tarek Hamed (Zamalek),");
		
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
					System.err.println(playersWithNoClubName.size() + " player(s) with no club for: " + line);
					for(Player noClubPlayer : playersWithNoClubName) {
						System.err.println(" - name = " + noClubPlayer.m_playerName);						
						noClubPlayer.setClubName("<Null club>");
					}
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
	FootballClub m_club;
	
	Player(String playerName, Team team, String group, Position position) {
		m_playerName = playerName;
		m_team = team;
		m_group = group;
		m_position = position;
		m_clubName = null;
	}
	
	void setClubName(String clubName) {
		m_clubName = clubName;
		FootballClub c = FootballClub.getClubFromName(clubName);
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
		FootballClub c = m_club == null ? FootballClub.DummyClub : m_club; 
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
		
		FootballClub c = m_club == null ? FootballClub.DummyClub : m_club; 
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

// http://www.fifa.com/worldcup/teams/index.html