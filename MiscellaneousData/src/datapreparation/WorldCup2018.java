package datapreparation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

import utils.*;

public class WorldCup2018 {

	public static void main(String args[]) {

		if (args.length == 0) {
			System.err.println("No file path provided");
			return;
		}

		String filePath = args[0];
		System.out.println("Reading from file: " + args[0]);
		
		List<Fixture> fixtures = readFromFile(filePath);
		
		// Produce CSV output
		String nl = System.lineSeparator();
		String header = Fixture.csvHeader();
		String body = fixtures.stream().map(f -> f.asCSV()).collect(Collectors.joining(nl));

		// Write to CSV file
		String outputFile = filePath.replaceAll(".txt", ".csv").replaceAll("src_data", "output_data");
		if(outputFile.equalsIgnoreCase(filePath)) {
			System.err.println("No output file produced - name clash with existing file");
			return;
		}
		else {
			String csvOutput = header + nl + body;
			FileWriter.writeUTF8File(outputFile, csvOutput);			
		}
	}
	
	static List<Fixture> readFromFile(String filePath) {
		
		List<Fixture> fixtures = new ArrayList<>();
		File f = new File(filePath);
		
		// Check the file is readable 
		if(!(f.exists() && f.isFile() && f.canRead())) { 
			System.err.println("Unable to read file " + filePath); 
		} 
		else {

			Round currentRound = null;
			
			try (Scanner sc = new Scanner(f)) { 
				while(sc.hasNext()) {
					String line = sc.nextLine().trim();
					if(line.length() == 0) continue;
					
					Round r = Round.convertToRound(line);
					if(r != null) {
						currentRound = r;
						System.out.println("Processing fixtures for round " + r.toString());
					}
					else if(currentRound == null) {
						System.err.println("No initial round set when processing: " + line);
						return fixtures;
					}
					else {
						Fixture fixture = Fixture.createNewFixture(currentRound,  line);
						if(fixture == null) {
							System.err.println("Failed to create fixture from: " + line);
						}
						else {
							fixtures.add(fixture);
							System.out.println(fixtures.size() + " : " + fixture.toString());
						}
					}					
				}
			} catch (FileNotFoundException e) {
				System.err.println("Failure reading file " + filePath + " : " + e.getMessage());
			}
			
			System.out.println("Read " + fixtures.size() + " fixtures");
		}
		
		return fixtures;
	}
	
}

enum Round {
	Group("Group stages"), 
	Last16("Last 16"), 
	QuarterFinals("Quarter-finals"), 
	SemiFinals("Semi-finals"), 
	ThirdPlace("Third-place play-off"), 
	Final("Final");
	
	String m_identifier;			// As used in source text
	Round(String identifier) {
		m_identifier = identifier;
	}

	// Convert identifier to an enum
	static Round convertToRound(String identifier) {
		Round found = null;
		for(Round r : Round.values()) {
			if(r.m_identifier.equalsIgnoreCase(identifier)) {
				found = r;
				break;
			}
		}
		
		return found;
	}
}

class Fixture {
	Round m_round;
	String m_description;
	
	String m_dayOfWeek;
	int m_dayOfMonth;
	int m_month;
	int m_year;
	int m_hourBST;
	int m_minuteBST;
	
	String m_team1;
	String m_team2;
	
	String m_groupID;	
	String m_location;
	
	String m_matchID;

	String m_error;
	
	static Fixture createNewFixture(Round round, String fixtureDescription) {
		Fixture f = null;
		switch(round) {
		case Group: f = createGroupFixture(round, fixtureDescription); break;
		case Last16: f = createLast16Fixture(round, fixtureDescription); break;
		case QuarterFinals: f = createQuarterFinalFixture(round, fixtureDescription); break;
		case SemiFinals: f = createSemiFinalFixture(round, fixtureDescription); break;
		case ThirdPlace: f = createThirdPlaceFixture(round, fixtureDescription); break;
		case Final: f = createFinalFixture(round, fixtureDescription); break;
//		default:
//			System.err.println("Unhandled round: " + round);
//			break;
		}

		if(f != null && !f.isComplete()) {
			System.err.println("Incomplete fixture: " + f.m_error + " : " + fixtureDescription);
			f = null;
		}
		return f;
	}
		
	// Fri June 15: Egypt v Uruguay (Group A) - Ekaterinburg, 1pm
	static Pattern s_groupMatchPattern = Pattern.compile("^(.*)\\s*:\\s*(.*)\\s+v\\s+(.*)\\s+\\(Group\\s+([A-H])\\s*\\)\\s+-\\s+(.*),\\s+(.*)$");
	
	static Fixture createGroupFixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_groupMatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "groupfixture");			
			String date = m.group(1);
			String team1 = m.group(2);
			String team2 = m.group(3);
			String groupID = m.group(4);
			String location = m.group(5);
			String time = m.group(6);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams(team1, team2);
			f.setLocation(location);
			f.setGroupID(groupID);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	// Sat June 30: 1C v 2D - Kazan, 3pm (Match 50)
	static Pattern s_last16MatchPattern = Pattern.compile("^(.*)\\s*:\\s*(.*)\\s+v\\s+(.*)\\s+-\\s+(.*),\\s+(.*)\\s+\\((Match \\d\\d)\\)$");
	
	static Fixture createLast16Fixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_last16MatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "last16fixture");			
			String date = m.group(1);
			String team1 = m.group(2);
			String team2 = m.group(3);
			String location = m.group(4);
			String time = m.group(5);
			String matchID = m.group(6);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams(team1, team2);
			f.setLocation(location);
			f.setMatchID(matchID);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	// Fri July 6: Winner match 49 v Winner match 50 - Nizhny Novgorod, 3pm (Match 57)
	static Pattern s_quarterFinalMatchPattern = Pattern.compile("^(.*)\\s*:\\s*(.*)\\s+v\\s+(.*)\\s+-\\s+(.*),\\s+(.*)\\s+\\((Match \\d\\d)\\)$");
	
	static Fixture createQuarterFinalFixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_quarterFinalMatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "qffixture");			
			String date = m.group(1);
			String team1 = m.group(2);
			String team2 = m.group(3);
			String location = m.group(4);
			String time = m.group(5);
			String matchID = m.group(6);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams(team1, team2);
			f.setLocation(location);
			f.setMatchID(matchID);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	// Tues July 10: Winner match 57 v Winner match 58 - St Petersburg, 7pm
	static Pattern s_semiFinalMatchPattern = Pattern.compile("^(.*)\\s*:\\s*(.*)\\s+v\\s+(.*)\\s+-\\s+(.*),\\s+(.*)$");
	
	static Fixture createSemiFinalFixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_semiFinalMatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "semifinalfixture");			
			String date = m.group(1);
			String team1 = m.group(2);
			String team2 = m.group(3);
			String location = m.group(4);
			String time = m.group(5);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams(team1, team2);
			f.setLocation(location);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	// Sat July 14: St Petersburg, 3pm
	static Pattern s_thirdPlaceMatchPattern = Pattern.compile("^(.*)\\s*:\\s+(.*),\\s+(.*)$");
	
	static Fixture createThirdPlaceFixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_thirdPlaceMatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "thirdplacefixture");			
			String date = m.group(1);
			String location = m.group(2);
			String time = m.group(3);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams("TBD", "TBD");
			f.setLocation(location);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	// Sun July 15: Moscow (Luzhniki), 4pm
	static Pattern s_finalMatchPattern = Pattern.compile("^(.*)\\s*:\\s+(.*),\\s+(.*)$");
	
	static Fixture createFinalFixture(Round round, String fixtureDescription) {
		boolean withDiagnostics = false;		
		Fixture f = null;
		Matcher m = s_finalMatchPattern.matcher(fixtureDescription);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "finalfixture");			
			String date = m.group(1);
			String location = m.group(2);
			String time = m.group(3);
			
			f = new Fixture(round, fixtureDescription);
			f.setDateTime(date, time);
			f.setTeams("TBD", "TBD");
			f.setLocation(location);
		}
		else {
			System.err.println("No regex match for : " + fixtureDescription);
		}
		
		return f;
	}
	
	Fixture(Round round, String description) {
		m_round = round;
		m_description = description;
		m_team1 = "";
		m_team2 = "";
		m_location = "";
		m_hourBST = -1;
		m_groupID = "";
		m_matchID = "";
		m_error = "";
	}

	public String toString() {
		return m_round.name() + " : " + 
				   (m_groupID.length() > 0 ? (m_groupID + " : ") : "") +  
					m_team1 + " v " + m_team2 + " : " + m_location +
					(m_matchID.length() > 0 ? (" : " + m_matchID) : "");
	}
	
	static Pattern s_dateMatchPattern = Pattern.compile("^(Mon|Tues|Wed|Thu|Fri|Sat|Sun)\\s+(June|July)\\s+(\\d?\\d$)");
	static Pattern s_timeMatchPattern = Pattern.compile("^(\\d?\\d)([ap]m)");

	void setDateTime(String date, String time) {		
		boolean withDiagnostics = false;		
		Matcher m = s_dateMatchPattern.matcher(date);
		if(m.matches()) {
			if(withDiagnostics) dumpMatches(m, "date");
			m_dayOfWeek = m.group(1);
			if(m_dayOfWeek.length() > 3) {
				m_dayOfWeek = m_dayOfWeek.substring(0,  3);
			}
			String month = m.group(2);
			if(month.equalsIgnoreCase("June")) {
				m_month = 6;
			}
			else if(month.equalsIgnoreCase("July")) {
				m_month = 7;
			}
			m_dayOfMonth = Integer.parseInt(m.group(3));
		}
		else {
			System.err.println("No regex match for date : " + date);
		}
		
		Matcher m2 = s_timeMatchPattern.matcher(time);
		if(m2.matches()) {
			if(withDiagnostics) dumpMatches(m, "time");			
			int hBST = Integer.parseInt(m2.group(1));
			String ampm = m2.group(2);
			if(ampm.equalsIgnoreCase("pm")) {
				hBST += 12;
			}
			
			m_hourBST = hBST;
			m_minuteBST = 0;		// All start on the hour			
		}

	}

	static void dumpMatches(Matcher m, String type) {		
		System.out.println("Found " + m.groupCount() + " " + type + " groups");
		for(int g = 1; g <= m.groupCount(); g++) {
			System.out.println(" - " + m.group(g));
		}
	}

	void setTeams(String team1, String team2) {
		m_team1 = team1;
		m_team2 = team2;
	}
	
	void setLocation(String location) {
		m_location = location;
	}
	
	void setGroupID(String groupID) {
		m_groupID = groupID;
	}

	void setMatchID(String matchID) {
		m_matchID = matchID;
	}

	boolean isComplete() {
		String error = "";
		if(m_team1.length() == 0) {
			error = "No team1";
		}
		else if(m_team2.length() == 0) {
			error = "No team2";
		}
		else if(m_dayOfWeek.length() == 0) {
			error = "No day of week";
		}
		else if(m_month == 0) {
			error = "No month";
		}
		else if(m_dayOfMonth == 0) {
			error = "No day of month";
		}
		else if(m_hourBST == -1) {
			error = "No time";			
		}
		else if(m_location.length() == 0) {
			error = "No location";			
		}
		else if((m_round == Round.Group) && (m_groupID.length() == 0)) {
			error = "No Group ID";			
		}
		
		if(error.length() > 0) {
			m_error = error;
		}
		
		return error.length() == 0;
	}

	static String comma = ",";
	
	static String csvHeader() {
		return "Round" + comma + 
				"Group" + comma +
				"Full round" + comma +
				"Day of week" + comma +
				"Date" + comma +
				"Time (BST)" + comma +
				"Real Teams" + comma +
				"Team1" + comma + 
				"Team2" + comma + 
				"Location" + comma +
				"Match ID";
	}
	
	String getDate() {
		Formatter fmt = new Formatter(); 
		String s = fmt.format("%02d/%02d/%04d", m_dayOfMonth, m_month, 2018).toString();
		fmt.close();
		return s;			
	}
	
	String getTimeBST() {
		Formatter fmt = new Formatter(); 
		String s = fmt.format("%02d:%02d", m_hourBST, m_minuteBST).toString();
		fmt.close();
		return s;			
	}
	
	String asCSV() {
		return CSV.protect(m_round.name()) + comma +
						m_groupID + comma +
						((m_round == Round.Group) ? m_round + " " + m_groupID : m_round) + comma +
						m_dayOfWeek + comma +
						getDate() + comma +
						getTimeBST() + comma +
						((m_round == Round.Group) ? "Y" : "N") + comma +
						CSV.protect(m_team1) + comma + 
						CSV.protect(m_team2) + comma + 
						CSV.protect(m_location) + comma +
						CSV.protect(m_matchID);
	}
	
}
