package footballEntities;

public enum InternationalTeam {
	
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

	public String m_name;
	public String m_geoCountryName;
	
	InternationalTeam() {
		m_name = name().replaceAll("_",  " ");
		m_geoCountryName = m_name;
	}
	
	InternationalTeam(String name, String geoCountryName) {
		m_name = name;
		m_geoCountryName = geoCountryName;
	}
	
	public String toString() {
		return m_name;
	}	
}
