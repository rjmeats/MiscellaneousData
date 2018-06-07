package datapreparation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;

import utils.CSV;
import utils.FileWriter;
import footballEntities.*;

public class WorldCup2018SquadsFifa {

	public static void main(String args[]) {

		if (args.length == 0) {
			System.err.println("No file path provided");
			return;
		}

		String filePath = args[0];
		System.out.println("Reading from file: " + args[0]);

		FootballClub.setUpMappings();

		FileReaderFifa reader = new FileReaderFifa();
		reader.readFromFile(filePath);
		
		DataHandlerFifa handler = new DataHandlerFifa(reader);
		handler.process();
	}	
}


class DataHandlerFifa {
	
	List<PlayerFifa> m_players;
	
	String m_sourceFilePath;
	
	DataHandlerFifa(FileReaderFifa reader) {
		m_players = reader.m_players;
		
		m_sourceFilePath = reader.m_filePath;
	}
	
	void process() {
		
		dumpCSV();
	}
	
	void dumpCSV() {
		String outputFile = m_sourceFilePath.replaceAll(".txt", ".csv").replaceAll("src_data", "output_data");
		if(outputFile.equalsIgnoreCase(m_sourceFilePath)) {
			System.err.println("No output file produced - name clash with existing file");
			return;
		}
		else {
			
			String nl = System.lineSeparator();
			String header = PlayerFifa.csvHeader();
			String body = m_players.stream().map(p -> p.asCSV()).collect(Collectors.joining(nl));
			
			String csvOutput = header + nl + body;
			FileWriter.writeUTF8File(outputFile, csvOutput);			
		}
	}
}


class FileReaderFifa {

	List<PlayerFifa> m_players;
	
	String m_filePath;
	
	FileReaderFifa() {
		m_players = new ArrayList<>();		
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
		
		if(line.startsWith("F1")) {
			// Headings line
		}
		else if(processDataLine(line)) {
			;
		}
		else {
			System.err.println("Warning: Line not recognised: " + line);
		}
	}

	// Argentina,5,MF,BIGLIA Lucas,30.01.1986 BIGLIA,AC Milan (ITA),175,73,1,WorldCup2018Squads/qcuxk3y7c1ezwo5yylnn.pdf
	// Country,ShirtNo,Position,Name,DOB Shirt name,Club (Cou),Height,Weight,const,const
	// 0       1       2        3    4              5          6      7      8     9

	static Pattern s_DOB_plus_shirtnamePattern = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4})\\s+(.*)");
	static Pattern s_club_plus_country = Pattern.compile("(.*)\\s+\\((...)\\)");
	
	boolean processDataLine(String line) {
		boolean OK = true;
		String fields[] = line.split(",");
		if(fields.length == 10) {
			PlayerFifa player = new PlayerFifa();
			try {				
				player.m_country = fields[0];
				player.m_shirtNo = Integer.parseInt(fields[1]);
				player.m_position = fields[2];
				player.m_playerName = fields[3];
				String DOB_plus_shirtname = fields[4];
				String club_plus_country = fields[5];
				player.m_height  = Integer.parseInt(fields[6]);
				player.m_weight  = Integer.parseInt(fields[7]);

				// Convert DOB_plus_shirtname into two separate fields
				player.m_DOB = "";
				player.m_DOB_day = 0;
				player.m_DOB_month = 0;
				player.m_DOB_year = 0;
				player.m_shirtName = "";

				Matcher m = s_DOB_plus_shirtnamePattern.matcher(DOB_plus_shirtname);
				if(!m.matches()) {
					OK = false;
					System.err.println("Unexpected format for DOB+shirt: " + DOB_plus_shirtname);
				}
				else {
					player.m_DOB = m.group(1) + "." + m.group(2) + "." + m.group(3);
					player.m_DOB_day = Integer.parseInt(m.group(1));
					player.m_DOB_month = Integer.parseInt(m.group(2));
					player.m_DOB_year = Integer.parseInt(m.group(3));
					
					player.calcAge(); 
					player.m_shirtName = m.group(4);
				}

				// Convert club_plus_country into two separate fields
				//    Club (Cou)
				player.m_clubName = "";
				player.m_clubCountry = "";
				
				m = s_club_plus_country.matcher(club_plus_country);
				if(!m.matches()) {
					OK = false;
					System.err.println("Unexpected format for club+country: " + club_plus_country);
				}
				else {
					player.m_clubName = m.group(1);
					player.m_clubCountry = m.group(2);

					// See if we know the name - often not as different variations. Not used, so just out of curiousity ...
					FootballClub fc = FootballClub.getClubFromName(player.m_clubName);
					if(fc == null) {
						if(player.m_clubName.endsWith(" FC")) {
							String clubName2 = player.m_clubName.replaceAll(" FC", ""); 
							fc = FootballClub.getClubFromName(clubName2);
						}
						
					}

					if(fc == null) {
						// System.out.println("Not Found club: " + clubName);								
					}
				}

				if(OK) {
					m_players.add(player);
				}
			} catch (Exception e) {
				System.err.println("Error parsing line: " + line + " : " + e.toString());
				OK = false;
			}
		}
		else {
			System.err.println("Unexpected number of fields (" + fields.length + ") : " + line);
			OK = false;
		}
				
		return OK;
	}	
}

class PlayerFifa {
	
	String m_country;
	int m_shirtNo;
	String m_position;
	String m_playerName;
	int m_height;
	int m_weight;

	// Convert DOB_plus_shirtname into two separate fields
	String m_DOB;		// dd.mm.yyyy
	int m_DOB_day;
	int m_DOB_month;
	int m_DOB_year;
	
	int m_ageAtStartOfWC2018;
	
	String m_shirtName;

	String m_clubName;
	String m_clubCountry;

	void calcAge() {
		// Age in years at start of tournament, 14th June 2018.
		int age = 2018 - m_DOB_year;
		if(m_DOB_month > 6 || ((m_DOB_month == 6) && (m_DOB_day > 14))) {
			age--;
		}
		
		m_ageAtStartOfWC2018 = age;
	}

	
	static String comma = ",";
	
	static String csvHeader() {
		return "National Team" + comma +
				"Position" + comma +
				"Club Name" + comma +
				"Club Country" + comma +
				"DOB" + comma +
				"Age" + comma +
				"Height" + comma +
				"Weight";
	}
	
	String asCSV() {
		
		return 	
				CSV.protect(m_country) + comma +
				CSV.protect(m_position) + comma +
				CSV.protect(m_clubName) + comma +
				CSV.protect(m_clubCountry) + comma +
				CSV.protect(m_DOB) + comma +
				m_ageAtStartOfWC2018 + comma + 
				m_height + comma +
				m_weight;
	}
}
