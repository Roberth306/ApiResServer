package clasesH;
// Generated 24-feb-2020 21:18:04 by Hibernate Tools 5.4.7.Final

/**
 * CarmanId generated by hbm2java
 */
public class CarmanId implements java.io.Serializable {

	private int idMano;
	private int idCarta;

	public CarmanId() {
	}

	public CarmanId(int idMano, int idCarta) {
		this.idMano = idMano;
		this.idCarta = idCarta;
	}

	public int getIdMano() {
		return this.idMano;
	}

	public void setIdMano(int idMano) {
		this.idMano = idMano;
	}

	public int getIdCarta() {
		return this.idCarta;
	}

	public void setIdCarta(int idCarta) {
		this.idCarta = idCarta;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof CarmanId))
			return false;
		CarmanId castOther = (CarmanId) other;

		return (this.getIdMano() == castOther.getIdMano()) && (this.getIdCarta() == castOther.getIdCarta());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getIdMano();
		result = 37 * result + this.getIdCarta();
		return result;
	}

}
