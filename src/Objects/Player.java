package Objects;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;

import GameInput.Input;
import GameInput.Keyboard;
import GamePolygons.Polygons;
import PhysicsEngine.CollisionDetection;
import Principal.Handler;
import Principal.Principal;

public class Player extends GameObject {
	
	//Tamanho
	private float width = 10, height = 10;
	//private Polygon playerPoly;
	//private Polygon shieldPoly;
	
	//antigo x e y antes do hitbox usado para corrigir a colis�o
	private double tx,ty; //Vari�veis temporarias
	private double ax = tx, ay = ty;
	
	//Temporario (para selecionar objeto)
	private GameObject tempOb; //GameObject tempor�rio para selecionar os objetos
	//private Rectangle tempRec; //Substituido por Polygon
	private Polygon tempPol;
	private Bonus bonus;
	
	Handler handler; //Onde est�o os objetos
	
	//GRAVIDADE
	private float gravity = 0.2f; //N�o � usado
	private final float MAX_SPEED = 5; //N�o � isado
	
	//Movimenta��o
	private float velocity = 2.4f; //Velociadade normal
	private float defaultVelocity = 2.4f; //Velocidade default usado para reiniciar o velocity
	//Se positivo, o player se movimenta de acordo com o boolean
	private boolean left = false;
	private boolean right = false;
	private boolean up = false;
	private boolean down = false;
	private boolean virarDireita = false, virarEsquerda = false;
	private boolean espaco = false;
	private float rotateVel = 6;
	
	//Arma
	private int tempoRecarregar = 20; //Em tickss d
	private int defTempoRecarregar = tempoRecarregar;
	//Boost
	private float boostRecarregar = 0.1f; //Taxa de recarregamento por tick
	private float BonusBoostTimer = 0; //Timer do boost, se positivo o bonus � usado at� acabar o tiemr
	private float boost = 2.7f; //Boost normal (sem bonus)
	private float defBoost = 2.7f; //Boost default � usado para reiniciar o boost
	private float boostValue = 1; //Valor do boost
	private float boostFuel = 110; //Boost Fuel � o valor da "gasolina" do boost, quanto maior, amis dura
	private float defBootFuel = 110; //Boost fuel default � usado para reiniciar o boostFuel
	Color shieldColor = new Color(0, 200, 255);
	
	//Shield
	private float shield = 50; //Valor do shield, se positivo, o player n�o morre
	private float defShield = shield; //Shield default, � usado para reiniciar a vari�vel shield
	
	private boolean colDetec = false; //Esse boolean de colis�o, ao ativado, corrigi a colis�o (usado para colis�o de Bloco)
	private boolean allColDet = false; //Ativa ao detectar qualquer colis�o
	private int pontos = 0;
	
	private boolean flag = false; //Usado para n�o crair mais de uma flag
	
	
	private GameObject colisao;
	
	public Player(Polygon p, ObjectId id) {
		super(p, id);
//	    xpoints = new double[] {x, x + width, x + width, x};
//	    ypoints = new double[] {y, y, y + height, y + height};
		//playerPoly = Principal.RectangleToPolygon(new Rectangle ((int)x, (int)y, (int)width, (int)height));
		//shieldPoly = Principal.RectangleToPolygon(new Rectangle ((int)x, (int)y, (int)width, (int)height));
		ty = getCenterY();
		tx = getCenterX();
		
		//Principal.scalePoints(shieldPoly, 1.1);
	}
	public Player(double x, double y, Polygon p, ObjectId id) {
		super(p, id);

		novaPosicao(x, y);
		ty = getCenterY();
		tx = getCenterX();
	}
	
	boolean corrigirColisao = false;
	
	int contador = 0;
	@Override
	public void tick(LinkedList<GameObject> object) {//Tick � chamado 60 vezes por segundo | 60 ticks = 1 segundo
		// TODO Auto-generated method stub
//		x = xpoints[0];
//		y = ypoints[0];
		//Detectou colis�o, ent�o corrigir colis�o
		if(colDetec) {
			CollisionDetection.corrigirColisao(this,colisao);
		}
		
		movimentacaoLogica();
		armaLogica();

		
		//Reseta colis�o
		allColDet = false;
		colDetec = false;
		
		
		controles(); //WASD ou Setas
		fisLogica(); //Inclui detectar colis�o
		
		setSuperArma(0);
		
		velocity = defaultVelocity * boostValue; //Velocidade = velocidade normal * velocidade do boost
		

		//N�o � usado
		falling = false;
		
		
		//N�o � usado
//		if(falling || jumping) {
//			vely += gravity;
//			
//			if(vely > MAX_SPEED)
//				vely = MAX_SPEED;
//		}
		
		

		//Adicionar Flag (Objetivo) depois do tempo objetivo que atualmente � 2000 ticks
		//EDIT: Isso n�o deve estar aqui!
		//talvez isso deve estar em algum tipo de gameEngine, mas n�o aqui! >:(
//		pontos++;
//		if(pontos > Handler.getObjetivo()) {
//			if(!flag)
//				object.add(new Flag(Polygons.getFlag(), ObjectId.Flag));
//			flag = true;
//		}
		//Armazenando coordenadas sem colis�o
		efeitos();
		contador++;
		if(contador > 60) {
			contador = 0;
		}
	}
	//A "novaPosicao(x[],y[], x, y)" define (obviamente) a nova posi��o do player,
	//mas diferente do translate, ele N�O adiciona posi��o, ele DEFINE uma nova
	//posi��o
	@Deprecated
	private void novaPosicao(double xpoints[], double ypoints[], double x, double y) {
		x = x - Principal.getCenterX(xpoints);
		y = y - Principal.getCenterY(ypoints);
		for (int i=0; i<xpoints.length; ++i) {
			xpoints[i] += x;
			ypoints[i] += y;
		}
	}
	private void movimentacaoLogica() {
		if(right) {
			addPosicao(velocity, 0);
		}
		if(left) {
			addPosicao(-velocity, 0);
		}
		if(up && !falling) {
			addPosicao(0, -velocity);
			//vely = -(velocity);
		}
		if(down) {
			addPosicao(0, velocity);
		}
		if(virarDireita) {
			rotatePolygon(rotateVel);
		}
		if(virarEsquerda) {
			rotatePolygon(-rotateVel);
		}
	}
	
	private void armaLogica() {
		if(tempoRecarregar <= 0) {
			if(espaco) {
				Principal.addObjHandler(new Bullet(getCenterX(), getCenterY(),Polygons.getBullet(), ObjectId.Bullet, angle));
				tempoRecarregar = defTempoRecarregar;
			}
		}
		//System.out.println(tempoRecarregar);
		tempoRecarregar--;
	}
	
	//Atualmente usado somente para detectar colis�o, usando o CollisionDetection.decColAdvanced(Polygon, Polygon)
	private void fisLogica() {
		//colDetec = false;
		
		//Seleciona o objeto armazenado no handler
		for(int i=0; i < handler.object.size(); i++) { 
			tempOb = handler.object.get(i); 
			
			
			if(tempOb.getId() == ObjectId.Block) { //se o objeto for um bloco
				tempPol = tempOb.getPolygon();
				if(CollisionDetection.decColAdvanced(tempPol, this.getPolygon())) { //Ent�o, detecta colis�o
					colDetec = true;
					allColDet = true;
					colisao = (GameObject)tempOb;
				}
			}
			else if(tempOb.getId() == ObjectId.AI) {
				tempPol = tempOb.getPolygon();
				
				//Se n�o tiver escudo, murreu!
				if(CollisionDetection.decColAdvanced(tempPol, this.getPolygon())) {
					allColDet = true;
					if(shield < 0) {
						System.out.println("Perdeu! :v");
						Finalizar(false);
						
					}
					else {
						shield--;
					}
				}
			}
			//BONUS
			else if(tempOb.getId() == ObjectId.Bonus) { 
				tempPol = tempOb.getPolygon();
				bonus = (Bonus) tempOb;
				if(CollisionDetection.decColAdvanced(tempPol, this.getPolygon())) {
					allColDet = true;
					if(bonus.getTipo() == 1){
						rechardeShield();
						handler.object.remove(i);
					}
					else if	(bonus.getTipo() == 2){
						superBoost(200, 3.5f);
						setSuperArma(200);
						handler.object.remove(i); 
					}
				}
			}
			else if(tempOb.getId() == ObjectId.Flag) {
				tempPol = tempOb.getPolygon();
				if(CollisionDetection.decColAdvanced(tempPol, this.getPolygon())) {
					allColDet = true;
					System.out.println("Voc� ganhou! :D \nPontos x2!");
					pontos *= 2;
					Finalizar(true);
				}
			}
			else if(tempOb.getId() == ObjectId.Asteroid) {
				if(CollisionDetection.decColAdvanced(tempOb.getPolygon(), this.getPolygon())) {
					allColDet = true;
					if(shield < 0) {
						System.out.println("Perdeu! :v");
						Finalizar(false);
						
					}
					else
						shield--;
				}
			}
					
		}
	}



	private void controles() {
		right = false;
		left = false;
		up = false;
		down = false;
		virarDireita = false;
		virarEsquerda = false;
		espaco = false;
		
		//WASD
		if(Principal.isPressedKey(68)) {right = true;}
		if(Principal.isPressedKey(65)) {left = true;}
		if(Principal.isPressedKey(87)) {up = true;}
		if(Principal.isPressedKey(83)) {down = true;}
		
		//Setas
		if(Principal.isPressedKey(39)) {right = true;}
		if(Principal.isPressedKey(37)) {left = true;}
		if(Principal.isPressedKey(38)) {up = true;}
		if(Principal.isPressedKey(40)) {down = true;}
		
		if(Principal.isPressedKey(76)) {virarDireita = true;}
		if(Principal.isPressedKey(75)) {virarEsquerda = true;}
		
		if(Principal.isPressedKey(32)) {espaco = true;}
		
		//Shift
		boost(Principal.isPressedKey(16));
	}
	
	//Usado ao finalizar
	private void Finalizar(boolean ganhou) {
		if(ganhou) {
			Principal.setTituloP("GANHOU! :D pontos(x2): " + String.valueOf(pontos));
			Principal.win();
		}
		else
			Principal.setTituloP("pontos: " + String.valueOf(pontos) + " | Objetivo: "); //+ Handler.getObjetivo());////////////////////////////////ARRUMAR
		
		Principal.setRunning(false);
	}
	
	public void rechardeShield() {
		shield = defShield;
		boostFuel = defBootFuel;
	}
	//Desenhar escudo - FAZER UM NOVO!!!!!
	private void shieldDraw(Graphics g) {
		int corR = 0;
		int cor = 0;
		if(shield > 10) {
			corR = 255;
			cor = 1;
		}
		shieldColor = new Color(corR, (int)(255*((shield)/defShield)*cor) ,(int)(255*((shield)/defShield)*cor));
		
		g.setColor(shieldColor);
		//g.fillPolygon(Principal.RectangleToPolygon(new Rectangle((int)x-2, (int)y-2, (int)width+4, (int)height+4))); //Desenhar player
		//novaPosicao(shieldPoly,(int)(x-(width*0.05)),(int)((y)-(height*0.05))); //-(height*0.05) e -(width*0.05) s�o gambiarras para corrigir a posi��o do escudo :)
		Polygon p = Principal.scalePoly(getPolygon(), 1.4);
		g.fillPolygon(p);
	}
	
	public void superBoost(float addDuracao, float potencia) {
		//boostFuel = defBootFuel;
		BonusBoostTimer += addDuracao;
		boost = potencia;
		
	}
	
	//BOOST (l�gica)
	private void boost(boolean set) {
		boostValue = 1;
		
		if(BonusBoostTimer > 0) {BonusBoostTimer--;} //Se tiver boost, diminui o tempo do boost
		else {boost = defBoost;} //Sen�o, boost = padr�o (default)
		
		if(set && boostFuel > 0) {
			boostValue = boostValue * boost;
			boostFuel -= 1;
		}
		else if (!set){
			if(boostFuel < defBootFuel) {
				boostFuel += boostRecarregar;
			}
			
		}
		

	}
	
	int superArmaDuracao = 0;
	public void setSuperArma(int addDuracao) {
		superArmaDuracao += addDuracao;
		if(superArmaDuracao > 0) {
			defTempoRecarregar = 5;
			superArmaDuracao--;
		}
		else {
			defTempoRecarregar = 10;
		}
	}
	
	//Desenhar boost
	private void boostDraw(Graphics g) {
		g.setColor(Color.green);
		g.fillRect((int)getPrimeiroX() ,(int)getPrimeiroY()+ (int)height + 5, (int)boostFuel/2, 2);
	}
	//Desenhar boost
	private void bonusBoostDraw(Graphics g) {
		g.setColor(Color.yellow);
		g.fillRect((int)getPrimeiroX() ,(int)getPrimeiroY()+ (int)height + 10, (int)BonusBoostTimer/2, 2);
	}
	
	
	
	@Override
	public void render(Graphics g) {
		
		
		//g.translate((int)x, (int)y);
		
		//g.fillRect((int)x+15, (int)y+15, (int)width, (int)height); //Desenhar player
		
		Graphics2D g2d = (Graphics2D)g;
		//g2d.rotate(30);

		
		

		efeitos(g);
		
		if(shield > 0)shieldDraw(g);// FAZER UM NOVO!!!!!
		bonusBoostDraw(g);
		if(allColDet) {g.setColor(Color.red);}else {g.setColor(Color.ORANGE);}
		g.fillPolygon(getPolygon()); //Desenhar player
		boostDraw(g);
		//g2d.drawString("X: " + x + "   Y: " + y,x,y-10);///
		
//		//Pontos de referencia
		g.setColor(Color.red);
		g.fillRect((int)getCenterX(),(int)getCenterY(), 2, 2);
		//g2d.drawString("    <- CENTRO :)", (int)getCenterX(), (int)getCenterY()+5);///
//		g.fillRect((int)p.getCenterX(),(int)p.getCenterY(), 2, 2);
//		g.fillRect((int)p.getCenterX(),(int)(p.getCenterY()+height/2), 2, 2);
		
		

//		
//		g.fillRect((int)100, (int)100, (int)width, (int)height);
//		g2d.translate(x, y);
//		g2d.rotate(angulo * Math.PI/180);
//		g2d.drawString("X: " + x + "   Y: " + y,100,100);g.fillRect((int)0, (int)0, (int)width, (int)height); //Desenhar player
//		g2d.rotate(-angulo * Math.PI/180);
//		g2d.translate(-x, -y);
		
		//g2d.rotate(-30);

	}
	

	//ArrayList e mais rapido que LinkedList?!?!?!?!
	/*
	 * Por algum motivo que eu ainda n�o entendi direito, nessa situa��o em espec�fico,
	 * o ArrayList tem uma performace muito superior ao LinkedList.
	 * Mas detalhe: O unico uso dessa armazenamento � adicionar um valor, e remover o primeiro,
	 * que � mais r�pido em ArrayList
	 */
//	static public LinkedList<Integer> efX = new LinkedList<Integer>();
//	static public LinkedList<Integer> efY = new LinkedList<Integer>();
	static public ArrayList<Integer> efX = new ArrayList<Integer>();
	static public ArrayList<Integer> efY = new ArrayList<Integer>();
	private void efeitos(Graphics g) {
		g.setColor(Color.MAGENTA);
		for(int i = 0; i< efX.size()-1; i++) {
			g.drawLine(efX.get(i),efY.get(i), efX.get(i+1), efY.get(i+1));
		}
	}
	int contadorEfeito = 0;
	private void efeitos() {
		contadorEfeito++;
		if(contadorEfeito >= 1) {
			if(efX.size() > 100) {
				efX.remove(0);
				efY.remove(0);
			}
			efX.add((int)getCenterX());
			efY.add((int)getCenterY());
			contadorEfeito = 0;
		}

	}
	@Override
	public Polygon getPolygon() {
		// TODO Auto-generated method stub
		//return Principal.RectangleToPolygon(new Rectangle ((int)x, (int)y, (int)width, (int)height));
		//return playerPoly;
		//Convers�o leeeeeenta....
		Polygon p = new Polygon();
		for (int i=0; i<xpoints.length; ++i) {
		     p.addPoint((int)xpoints[i], (int)ypoints[i]);
		}
		
		
		//return new Polygon(intArrayX,intArrayY, intArrayY.length);
		return p;
	}
	
	private int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}

}
