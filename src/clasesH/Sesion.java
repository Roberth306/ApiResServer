package clasesH;
// Generated 21-feb-2020 17:01:37 by Hibernate Tools 5.4.7.Final

/**
 * Sesion generated by hbm2java
 */
public class Sesion implements java.io.Serializable {

	private String idSesion;
	private Jugadores jugadores;

	public Sesion() {
	}

	public Sesion(String idSesion, Jugadores jugadores) {
		this.idSesion = idSesion;
		this.jugadores = jugadores;
	}

	public String getIdSesion() {
		return this.idSesion;
	}

	public void setIdSesion(String idSesion) {
		this.idSesion = idSesion;
	}

	public Jugadores getJugadores() {
		return this.jugadores;
	}

	public void setJugadores(Jugadores jugadores) {
		this.jugadores = jugadores;
	}

}
