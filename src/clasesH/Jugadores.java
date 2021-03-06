package clasesH;
// Generated 24-feb-2020 21:18:04 by Hibernate Tools 5.4.7.Final

import java.util.HashSet;
import java.util.Set;

/**
 * Jugadores generated by hbm2java
 */
public class Jugadores implements java.io.Serializable {

	private String usuario;
	private String password;
	private Set partidas = new HashSet(0);
	private Set sesions = new HashSet(0);

	public Jugadores() {
	}

	public Jugadores(String usuario, String password) {
		this.usuario = usuario;
		this.password = password;
	}

	public Jugadores(String usuario, String password, Set partidas, Set sesions) {
		this.usuario = usuario;
		this.password = password;
		this.partidas = partidas;
		this.sesions = sesions;
	}

	public String getUsuario() {
		return this.usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set getPartidas() {
		return this.partidas;
	}

	public void setPartidas(Set partidas) {
		this.partidas = partidas;
	}

	public Set getSesions() {
		return this.sesions;
	}

	public void setSesions(Set sesions) {
		this.sesions = sesions;
	}

}
