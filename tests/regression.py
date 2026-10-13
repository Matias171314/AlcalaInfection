from pathlib import Path
import tempfile,subprocess,re
root=Path(__file__).resolve().parents[1]
# Sustituye únicamente la vista para comprobar la lógica sin una pantalla gráfica.
jars=list((Path.home()/'.m2/repository/org/netbeans/external/AbsoluteLayout').glob('**/*.jar'))
if not jars:
 jars=[Path('/snap/netbeans/current/netbeans/java/modules/ext/AbsoluteLayout.jar')]
if not jars[0].is_file():
 raise SystemExit('Compila primero el proyecto en NetBeans para descargar AbsoluteLayout.')
with tempfile.TemporaryDirectory(prefix='alcala-check-') as t:
 p=Path(t)
 src=root/'src/main/java/user/apocalipsiszombi2025/gui/InterfazManager.java'
 methods=re.findall(r'public (?:synchronized )?void (\w+)\(([^)]*)\)',src.read_text())
 fake=p/'InterfazManager.java'
 fake.write_text('package user.apocalipsiszombi2025.gui; public class InterfazManager { public InterfazManager(PantallaPrincipal gui) {} '+ ' '.join('public void '+name+'('+args+') {}' for name,args in methods)+'}')
 harness=p/'RegressionChecks.java'
 harness.write_text('''import user.apocalipsiszombi2025.*;
import user.apocalipsiszombi2025.gui.*;
import user.apocalipsiszombi2025.util.*;
import java.util.*;
import java.util.concurrent.atomic.*;
public class RegressionChecks {
 static Object field(Object o, String name) throws Exception { var f=o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o); }
 static void check(boolean value, String name) { if(!value) throw new AssertionError(name); System.out.println("PASS: "+name); }
 static Thread task(Runnable r) { Thread t=new Thread(r); t.start(); return t; }
 static void until(java.util.function.BooleanSupplier s) throws Exception { long end=System.nanoTime()+2_000_000_000L; while(!s.getAsBoolean() && System.nanoTime()<end) Thread.sleep(5); check(s.getAsBoolean(),"espera con límite"); }
 public static void main(String[] args) throws Exception {
  var gui=new InterfazManager(null); var zona=new Exterior(0,gui); var refugio=new Refugio(gui); var zonas=new Exterior[]{zona};
  var h=new Humano("H0001",new Tunel[0],refugio,zonas,gui); var z=new Zombi("Z0000",zonas,gui,0);
  ApocalipsisZombi2025.pausarSimulacion();
  var pausa=task(() -> {try {ApocalipsisZombi2025.esperarReanudacion();} catch(InterruptedException e) {}});
  until(() -> pausa.getState()==Thread.State.WAITING); ApocalipsisZombi2025.reanudarSimulacion(); pausa.join(1000); check(!pausa.isAlive(),"reanudar despierta al hilo");
  check(Config.humanoAIdZombi("H0099").equals("Z0099"),"formato de ID zombi desde Config");
  var otraZona=new Exterior(1,gui); var viajero=new Zombi("ZVIAJE",new Exterior[]{zona,otraZona},gui,0);
  zona.registrarZombi(viajero);
  var plazo=Zombi.class.getDeclaredField("tiempoSinVictimaRestante");plazo.setAccessible(true);plazo.setLong(viajero,50);
  var patrullar=Zombi.class.getDeclaredMethod("patrullarZona");patrullar.setAccessible(true);
  ApocalipsisZombi2025.pausarSimulacion();
  var errorViaje=new AtomicReference<Throwable>();
  var viaje=task(() -> {try {patrullar.invoke(viajero);} catch(Exception e) {errorViaje.set(e);} });
  until(() -> viaje.getState()==Thread.State.WAITING);Thread.sleep(100);
  check(viajero.getZonaActual()==0 && plazo.getLong(viajero)==50,"pausa no consume plazo de cambio");
  ApocalipsisZombi2025.reanudarSimulacion();viaje.join(1000);
  check(!viaje.isAlive() && errorViaje.get()==null && viajero.getZonaActual()==1,"sin víctima cambia a una zona distinta");
  check(zona.getNumZombis()==0 && otraZona.getNumZombis()==1,"movimiento traslada los contadores");
  check(plazo.getLong(viajero)>=Config.TIEMPO_CAMBIO_ZONA_MIN && plazo.getLong(viajero)<=Config.TIEMPO_CAMBIO_ZONA_MAX,"nuevo plazo respeta Config");
  otraZona.retirarZombi(viajero);
  plazo.setLong(z,1);patrullar.invoke(z);check(z.getZonaActual()==0,"una sola zona no produce cambio inválido");zona.retirarZombi(z);
  var zonaConVictima=new Exterior(0,gui) { @Override public boolean atacar(Zombi atacante) {return true;} };
  var ocupado=new Zombi("ZOCUPADO",new Exterior[]{zonaConVictima,otraZona},gui,0);plazo.setLong(ocupado,1);
  patrullar.invoke(ocupado);check(ocupado.getZonaActual()==0 && plazo.getLong(ocupado)>=Config.TIEMPO_CAMBIO_ZONA_MIN,"encontrar víctima reinicia plazo sin cambiar zona");
  for(int i=0;i<10;i++) zona.atacar(z); check(zona.getNumZombis()==1,"ataques sin víctimas no duplican zombis"); zona.retirarZombi(z); check(zona.getNumZombis()==0,"retirada actualiza población");
  refugio.registrarEntrada(h); refugio.registrarEntrada(h); check(refugio.getHumanosEnRefugio()==1,"entrada idempotente"); refugio.salirRefugio(h); check(refugio.getHumanosEnRefugio()==0,"salida descuenta refugio");
  var h2=new Humano("H0002",new Tunel[0],refugio,zonas,gui);
  var c1=task(() -> {try {refugio.entrarZonaComedor(h);} catch(InterruptedException e) {}});
  var c2=task(() -> {try {refugio.entrarZonaComedor(h2);} catch(InterruptedException e) {}});
  AtomicInteger comida=(AtomicInteger)field(refugio,"comidaDisponible"); until(() -> comida.get()==48); check(c1.isAlive()&&c2.isAlive(),"dos humanos comen simultáneamente"); c1.interrupt();c2.interrupt();c1.join();c2.join();
  ((List<Humano>)field(zona,"humanos")).add(h);
  ApocalipsisZombi2025.pausarSimulacion();
  var atacante=task(() -> {try {zona.atacar(z);} catch(InterruptedException e) {}}); until(h::estaSiendoAtacado);
  var z2=new Zombi("Z0002",zonas,gui,0); var atacante2=task(() -> {try {zona.atacar(z2);} catch(InterruptedException e) {}}); atacante2.join(500);
  check(!atacante2.isAlive(),"víctima reservada impide segundo ataque"); atacante.interrupt();atacante.join();check(!h.estaSiendoAtacado(),"interrupción libera víctima");
  ApocalipsisZombi2025.reanudarSimulacion();
  var zonaMuerte=new Exterior(1,gui) { @Override public void explorar(Humano humano) {humano.setVivo(false);} };
  var refugioRapido=new Refugio(gui) { @Override public void entrarZonaComun(Humano humano) {registrarEntrada(humano);} };
  var tunelRapido=new Tunel(1,gui) {
   @Override public void salirEnGrupo(Humano humano) {}
   @Override public void entrarRefugio(Humano humano) {throw new AssertionError("muerto vuelve al refugio");}
  };
  var muerto=new Humano("H0099",new Tunel[]{tunelRapido},refugioRapido,new Exterior[]{zona,zonaMuerte},gui);
  muerto.start();muerto.join(1000);check(!muerto.isAlive(),"muerto termina sin regresar al refugio");until(() -> zonaMuerte.getNumZombis()==1);
  check(ApocalipsisZombi2025.getTopZombis().size()==1,"conversión registra un único zombi en la zona de muerte");
  ApocalipsisZombi2025.pausarSimulacion();
  var f=ApocalipsisZombi2025.class.getDeclaredField("humanos");f.setAccessible(true);((List<Humano>)f.get(null)).add(h);
  h.start(); until(() -> h.getState()==Thread.State.WAITING); ApocalipsisZombi2025.apagarSimulacion();check(!h.isAlive(),"apagado interrumpe humanos pausados"); check(ApocalipsisZombi2025.getTopZombis().size()==1,"apagado no convierte humanos en zombis"); check(zonaMuerte.getNumZombis()==0,"apagado retira zombis de las zonas");
 }
}''')
 cp=str(jars[0])
 sources=[str(f) for f in (root/'src/main/java').rglob('*.java') if f != src]
 subprocess.run(['javac','--release','23','-cp',cp,'-d',t,*sources,str(fake),str(harness)],check=True)
 subprocess.run(['java','-cp',t+':'+cp,'RegressionChecks'],cwd=t,check=True,timeout=20)
