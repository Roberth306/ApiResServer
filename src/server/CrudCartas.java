package server;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.query.Query;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import clasesA.CartaJugada;
import clasesA.SupCarta;
import clasesH.Carman;
import clasesH.CarmanId;
import clasesH.Cartas;
import clasesH.Jugadores;
import clasesH.Mano;
import clasesH.Partida;
import clasesH.Sesion;

import org.apache.catalina.webresources.TomcatJarInputStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import hibernateUtil.HibernateUtil;

@Path("/inicio")
public class CrudCartas {

	private final int terminadoFalse = 0;
	private final int terminadoTrue = 1;
	private enum Caracteristica {Motor, Potencia, Velocidad, Cilindros, Revoluciones, Consumo}

	private final int mPlayer = 1;
	private final int mCPU = 0;

	private SessionFactory sFactory;
	private Session session;
	private Transaction transaction;
	private String sql = "";

	@GET
	@Path("/saluda")
	public String saluda() {
		return "Bienvenido!!";
	}


	@Path("/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(String json) {

		String email;
		String password;
		Gson gson = new Gson();
		Jugadores j1;

		sFactory = HibernateUtil.getSessionFactory();
		session = sFactory.openSession();
		Response res = Response.ok().build();


		j1 = new Gson().fromJson(json, Jugadores.class);

		System.out.println(j1.getUsuario()+" "+j1.getPassword());


		Query q = session.createQuery("from Jugadores where Usuario = :email and Password = :password");
		q.setParameter("email", j1.getUsuario());
		q.setParameter("password", j1.getPassword());
		ArrayList<Jugadores> jugador = (ArrayList<Jugadores>)q.getResultList();
		if(!jugador.isEmpty()) {
			
			Query q2 = session.createQuery("from Sesion where Email = :email");
			q2.setParameter("email", j1.getUsuario());

			ArrayList<Sesion> sesion = (ArrayList<Sesion>)q2.getResultList();

			if(!sesion.isEmpty()) {
				res = Response.ok(new Gson().toJson(sesion.get(0).getIdSesion())).build();

			}else {
				String sesionUID = UUID.randomUUID().toString();
				transaction = session.beginTransaction();
				session.save(new Sesion(sesionUID, jugador.get(0)));
				transaction.commit();
				res = Response.ok(sesionUID).build();
			}

		}else {
			res = Response.status(Status.BAD_REQUEST).entity("Usuario/Contraseña Incorrecto").build();
		}
		session.close();
		return res;

	}

	@Path("/partida")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response partidaNueva(String json) {

		Sesion s1 = new Gson().fromJson(json, Sesion.class);
		Response res = Response.serverError().build();

		System.out.println(s1.getIdSesion());
		if(revisarSesion(s1.getIdSesion())) {
			System.out.println("Encontrado");

			sFactory = HibernateUtil.getSessionFactory();
			session = sFactory.openSession();

			Query sql = session.createQuery("from Sesion where Id_Sesion = :idSesion");
			sql.setParameter("idSesion", s1.getIdSesion());

			Sesion lSesion = (Sesion)sql.getSingleResult();

			Query sql2 = session.createQuery("from Partida where Email = :email and Terminado = :terminado");
			sql2.setParameter("email", lSesion.getJugadores().getUsuario());
			sql2.setParameter("terminado", terminadoFalse);

			ArrayList<Partida> lPartida = (ArrayList<Partida>)sql2.getResultList();

			if(!lPartida.isEmpty()) {
				res = Response.status(Status.NOT_FOUND).entity(new Gson().toJson(lPartida.get(0).getIdGame())).build();
			}else {

				//Inicializamos partida y manos
				Partida partida = new Partida(lSesion.getJugadores(), false);
				Mano manoJ = new Mano(partida, true);
				Mano manoC = new Mano(partida, false);

				//Guardamos partida y manos en la BD
				transaction = session.beginTransaction();
				session.save(partida);
				session.save(manoJ);
				session.save(manoC);
				transaction.commit();

				//Creamos las Cartas de Player y CPU
				ArrayList<Cartas> cartasJugador = new ArrayList<Cartas>();
				ArrayList<Cartas> cartasCPU = new ArrayList<Cartas>();
				ArrayList<Cartas> baraja = new ArrayList<Cartas>();
				ArrayList<SupCarta> cartaJs = new ArrayList<SupCarta>();

				//Damos cartas a Player y CPU
				baraja = obtenerCartas(baraja);
				System.out.println(baraja.isEmpty());
				repartirCartas(cartasJugador, baraja);
				repartirCartas(cartasCPU, baraja);

				//Guardamos en la BD las manos de ambos Jugadores
				for(int i=0;i<6;i++) {
					CarmanId idJ = new CarmanId(manoJ.getIdMano(), cartasJugador.get(i).getIdCarta());
					CarmanId idC = new CarmanId(manoC.getIdMano(), cartasCPU.get(i).getIdCarta());

					Carman carmanJ = new Carman(idJ, cartasJugador.get(i), manoJ, false);
					Carman carmanC = new Carman(idC, cartasCPU.get(i), manoC, false);

					transaction = session.beginTransaction();
					session.save(carmanJ);
					session.save(carmanC);
					transaction.commit();

					cartaJs.add(new SupCarta(cartasJugador.get(i).getIdCarta(), cartasJugador.get(i).getMarca(),
							cartasJugador.get(i).getModelo(), cartasJugador.get(i).getMotor(),cartasJugador.get(i).getPotencia(), cartasJugador.get(i).getVelocidad(), 
							cartasJugador.get(i).getCilindros(), cartasJugador.get(i).getRevoluciones(), cartasJugador.get(i).getConsumo()));
				}
				res = Response.ok().entity(new Gson().toJson(cartaJs)).build();
			}

		}else {
			res = Response.status(Status.NOT_FOUND).entity("Sesion Caducada").build();
			System.out.println("Inventado");
		}
		session.close();
		return res;
	}

	@Path("/cancelarP")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response cancelarPartida(String orden) { 
		

		Partida p1 = new Gson().fromJson(orden, Partida.class);

		sFactory = HibernateUtil.getSessionFactory();
		session = sFactory.openSession();

		transaction = session.beginTransaction();
		Query sql = session.createQuery("update Partida set Terminado = :terminado where Id_game = :idGame");
		sql.setParameter("terminado", 1);
		sql.setParameter("idGame", p1.getIdGame());
		sql.executeUpdate();

		transaction.commit();
		session.close();
		return Response.ok().build();
	}

	/*@Path("/turno")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response elegirTurno() {

		TODO implemetar turnos
	}*/

	@Path("/jugarP")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response jugarPartida(String json) {
		Response res = Response.status(Status.BAD_REQUEST).build();

		CartaJugada cP = new Gson().fromJson(json, CartaJugada.class);
		


		if(revisarSesion(cP.getIdSesion())) {
			Random rnd = new Random();
			Mano manoC;
			Mano manoJ;
			ArrayList<Carman> cartasC;
			Cartas cartaJ;
			Cartas cartaC;
			
			sFactory = HibernateUtil.getSessionFactory();
			session = sFactory.openSession();

			//Obtenemos la mano de la CPU y Jugador segun el Juego
			Query sqlManoC = session.createQuery("from Mano where Id_Game = :idGame and Player = :isPlayer");
			sqlManoC.setParameter("idGame", cP.getIdGame());
			sqlManoC.setParameter("isPlayer", mCPU);
			manoC = (Mano) sqlManoC.getSingleResult();

			Query sqlManoJ = session.createQuery("from Mano where Id_Game = :idGame and Player = :isPlayer");
			sqlManoJ.setParameter("idGame", cP.getIdGame());
			sqlManoJ.setParameter("isPlayer", mPlayer);
			manoJ = (Mano) sqlManoJ.getSingleResult();

			//Cargamos las cartas de la CPU a partir de la mano de la partida actual
			Query sqlCartaC = session.createQuery("from Carman where Id_Mano = :idMano and Usada = :usada");
			sqlCartaC.setParameter("idMano", manoC.getIdMano());
			sqlCartaC.setParameter("usada", 0);
			cartasC = (ArrayList<Carman>)sqlCartaC.getResultList();

			//Cargamos la carta Jugador y CPU
			Query sCartaC = session.createQuery("from Cartas where Id_Carta = :idCarta");
			sCartaC.setParameter("idCarta", cartasC.get(cartaRandom(cartasC.size())).getId().getIdCarta());
			cartaC = (Cartas) sCartaC.getSingleResult();

			Query sCartaJ = session.createQuery("from Cartas where Id_Carta = :idCarta");
			sCartaJ.setParameter("idCarta", cP.getIdCarta());
			cartaJ = (Cartas) sCartaJ.getSingleResult();

			//Pasamos el estado de las cartas a usadas
			transaction = session.beginTransaction();
			Query cUsadaC = session.createQuery("update Carman set Usada = :usada where Id_Mano = :idMano and Id_Carta = :idCarta");
			cUsadaC.setParameter("usada", 1);
			cUsadaC.setParameter("idMano", manoC.getIdMano());
			cUsadaC.setParameter("idCarta", cartaC.getIdCarta());
			cUsadaC.executeUpdate();
			transaction.commit();

			transaction = session.beginTransaction();
			Query cUsadaJ = session.createQuery("update Carman set Usada = :usada where Id_Mano = :idMano and Id_Carta = :idCarta");
			cUsadaJ.setParameter("usada", 1);
			cUsadaJ.setParameter("idMano", manoJ.getIdMano());
			cUsadaJ.setParameter("idCarta", cartaJ.getIdCarta());
			cUsadaJ.executeUpdate();
			transaction.commit();
			
			System.out.println(manoJ.getIdMano() + " "+ cartaJ.getIdCarta());
			System.out.println(manoC.getIdMano() + " "+ cartaC.getIdCarta());


			//Hacemos la jugada
			switch(cP.getCaracteristica()) {
			case "Motor":
				if(cartaC.getMotor()>=cartaJ.getMotor()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			case "Potencia":
				if(cartaC.getPotencia()>=cartaJ.getPotencia()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			case "Velocidad":
				if(cartaC.getVelocidad()>=cartaJ.getVelocidad()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			case "Cilindros":
				if(cartaC.getCilindros()>=cartaJ.getCilindros()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			case "Revoluciones":
				if(cartaC.getRevoluciones()<=cartaJ.getRevoluciones()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			case "Consumo":
				if(cartaC.getConsumo()<=cartaJ.getConsumo()) {
					res = Response.ok().entity(new Gson().toJson(false)).build();
				}else {
					res = Response.ok().entity(new Gson().toJson(true)).build();
				}
				break;
			}
		}else {
			res = Response.status(Status.NOT_FOUND).entity("Sesion Caducada").build();
		}


		session.close();
		return res;
	}

	private int cartaRandom(int total) {
		Random rnd = new Random();

		return rnd.nextInt(total);
	}

	private void repartirCartas(ArrayList<Cartas> mano, ArrayList<Cartas> baraja) {
		Random rnd = new Random();

		ArrayList<Cartas> cartas;

		System.out.println(baraja.size());
		int elegido;

		for(int i = 0; i<6; i++) {
			elegido = rnd.nextInt(baraja.size());

			mano.add(baraja.get(elegido));

			baraja.remove(elegido);

		}
	}

	private ArrayList<Cartas> obtenerCartas(ArrayList<Cartas> baraja) {

		sFactory = HibernateUtil.getSessionFactory();
		session = sFactory.openSession();

		Query sql = session.createQuery("from Cartas");

		baraja = (ArrayList<Cartas>)sql.getResultList();
		System.out.println(baraja.isEmpty());
		//session.close();

		return baraja;
	}

	private boolean revisarSesion(String jSesion) {
		boolean existe = false;
		sFactory = HibernateUtil.getSessionFactory();
		session = sFactory.openSession();

		Query q2 = session.createQuery("from Sesion where Id_Sesion = :idSesion");
		q2.setParameter("idSesion", jSesion);

		ArrayList<Sesion> sesion = (ArrayList<Sesion>)q2.getResultList();

		if(!sesion.isEmpty()) {
			existe = true;
		}else {
			existe = false;
		}

		session.close();
		return existe;
	}

}
