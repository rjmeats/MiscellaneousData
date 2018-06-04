package datapreparation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum FootballClub {

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
	Leicester_City("UK"),
	Leeds_United("UK"),
	Liverpool("UK"),
	Manchester_City("UK"),
	Manchester_United("UK"),
	Middlesbrough("UK"),
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
	Hibernian("Edinburgh", "UK"),
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
	Malaga("Spain"),
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
	SV_Sandhausen("Heidelberg", "Germany"),
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
	Valenciennes_FC("France"),
	
	Benfica("Lisbon", "Portugal"),
	Braga("Portugal"),
	CD_Feirense("Santa Maria da Feira", "Portugal"),		// Just Feira in Tableau
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
	Lokeren("Belgium"),
	Ostende("Belgium"),
	Standard_Liege("Liege", "Belgium"),
	Waasland_Beveren("Waasland-Beveren", "Beveren", "Belgium"),
	
	Red_Bull_Salzburg("Salzburg", "Austria"),
	
	FC_Basel("Switzerland"),
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
		
	Valur("Reykjavik", "Iceland"),
	Vikingur("Leirvik", "Faroe Islands"),		// Not known by Tableau	Lat/Long = 62.211111 / -6.706111	
	
	Gornik_Zabrze("Zabrze", "Poland"),
	Jagiellonia_Bialystok("Bialystok", "Poland"),
	Lechnia_Gdansk("Gdansk", "Poland"),
	Legia_Warsaw("Warsaw", "Poland"),

	Dunajska_Streda("Slovakia"),				// Not known by Tableau	Lat/Long = 47.994444 / 17.619444
	
	Dinamo_Bucharest("Bucharest", "Romania"),
	
	Levski_Sofia("Sofia", "Bulgaria"),
	Lokomotiv_Plovdiv("Plovdiv", "Bulgaria"),
	Ludogorets_Razgrad("Razgrad", "Bulgaria"),
	
	Partizan_Belgrade("Belgrade", "Serbia"),
	Red_Star_Belgrade("Belgrade", "Serbia"),
	
	Dinamo_Zagreb("Zagreb", "Croatia"),
	Hajduk_Split("Split", "Croatia"),
	NK_Lokomotiva("Zagreb", "Croatia"),
	Rijeka("Croatia"),
	
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
	Antalyaspor("Antalya", "Turkey"),
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
	FC_Tokyo("Japan"),
	Urawa_Reds("Saitama", "Japan"),
	Vissel_Kobe("Kobe", "Japan"),
	Urawa_Red_Diamonds("Saitama", "Japan"),
	Yokohama_F_Marinos("Yokohama", "Japan"),
	
	Asan_Mugunghwa_FC("Asan", "South Korea"),
	Daegu_FC("South Korea"),
	FC_Seoul("South Korea"),
	Incheon_United("South Korea"),
	Jeju_United("South Korea"),
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
	ES_Tunis("Esperance", "Tunis", "Tunisia"),
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
	Leon("Mexico"),
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
	Atletico_Junior("Barranquilla", "Colombia"),
	Once_Caldas("Manizales", "Colombia"),
	Patriotas("Tunja", "Colombia"),
	Rionegro_Aguilas("Rionegro", "Colombia"),
	Santa_Fe("Bogota", "Colombia"),
	
	Olimpia("Asuncion", "Paraguay"),
	
	Alianza_Lima("Lima", "Peru"),
	Deportivo_Municipal("Lima", "Peru"),
	Melgar("Arequipa", "Peru"),
	Sports_Boys("Callao", "Peru"),
	Union_Comercio("Nueva Cajamarca", "Peru"),
	Universidad_San_Martin("Lima", "Peru"),
	Universitario("Lima", "Peru"),
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
	
	FootballClub(String country) {
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

	FootballClub(String town, String country) {
		m_clubName = name().replaceAll("_",  " ");
		m_town = town;
		m_possibleShortName = "";
		m_geoCountryName = country;				
	}
	
	FootballClub(String clubName, String town, String country) {
		m_clubName = clubName;
		m_town = town;
		m_possibleShortName = "";
		m_geoCountryName = country;				
	}
	
	String getClubName() {
		return m_clubName;
	}
	
	private static HashMap<String, FootballClub> s_nameMappings = new HashMap<>();
	private static Set<String> s_conflictNames = new HashSet<>();
	
	static void setUpMappings() {
		s_nameMappings = new HashMap<>();
		s_conflictNames = new HashSet<>();
		for(FootballClub c : FootballClub.values()) {
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
		addMapping("Al Ahli Riyadh", Al_Ahli);				// Typo in BBC page ?	Team is in Jeddah
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
		addMapping("Junior-Colombia", Atletico_Junior);
		addMapping("Ajax Amsterdam", Ajax);
		addMapping("Al-Ittifaq", Al_Ettifaq);
		addMapping("Al Ittifaq", Al_Ettifaq);
		addMapping("Alliance", Alianza);
		addMapping("Alsaad", Al_Sadd);
		addMapping("LOSC", Lille_OSC);
		addMapping("Lille", Lille_OSC);
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
		addMapping("Borussia Mönchengladbach", Borussia_Monchengladbach);
		addMapping("Jeonbuk Hyundai Motors", Jeonbuk_Hyundai);
		addMapping("1899 Hoffenheim", Hoffenheim);
		addMapping("Dynamo Kiev", Dynamo_Kyiv);
		addMapping("FC Red Bull Salzburg", Red_Bull_Salzburg);
		addMapping("Leipzig", RB_Leipzig);
		addMapping("Junior", Atletico_Junior);
		addMapping("Cercle Brugge KSV", Cercle_Brugge);
		addMapping("Lobos Buap", Lobos_BUAP);
		addMapping("SPAL 2013", SPAL);
		addMapping("Vitoria Guimaraes", Vitoria_de_Guimaraes);
		
		// Remove conflicts
		s_conflictNames.stream().forEach(n -> s_nameMappings.remove(n));
	}
	
	static void addMapping(String name, FootballClub c) {
		if(s_nameMappings.containsKey(name)) {
			FootballClub c2 = s_nameMappings.get(name);
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
	
	public static FootballClub getClubFromName(String name) {
		return s_nameMappings.get(name.replaceAll("\\.", ""));
	}
}
