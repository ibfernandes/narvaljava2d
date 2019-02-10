package demo;

import java.awt.Rectangle;
import java.util.ArrayList;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.utilities.Timer;

public class Benchmark {
	
	public static void main(String args[]) {
		System.out.println(normalize(-1f,-0.5f,-1.5f, 0,1));	
		
	}
	
	public static float normalize(float xmin, float xmax, float x, float a, float b) {
		//TODO: should min max in a,b
		return (b-a) * (x - xmin)/(xmax - xmin) + a;
	}
	
	public static void iteratingTest() {
		int numberOfTests = 100;
		int entityQuantity = 5000000;
		ArrayList<Entity> ents = new ArrayList<>();
		Entity entsArray[] = new Entity[entityQuantity];
		EntityManager em = new EntityManager();

		//Fill arrayList
		for(int i=0; i < entityQuantity;i++) {
			ents.add(em.newEntity());
		}
		
		for(int i=0; i < entityQuantity;i++) {
			entsArray[i] = em.newEntity();
		}
		
		long avg1 =0;
		long avg2 =0;
		long avg3 =0;
		for(int k=0; k<numberOfTests;k++) {
			//Tests Performance of for each
			long start = System.nanoTime();
			for(Entity e: ents) {
				e.setName("teste");
			}
			long elapsed = System.nanoTime() - start;
			avg1+=elapsed;
			
			//Tests Performance of C-type for
			start = System.nanoTime();
			int size = ents.size();
			for(int i=0; i<size;i++) {
				ents.get(i).setName("teste");
			}
			elapsed = System.nanoTime() - start;
			avg2+=elapsed;
			
			//Tests Performance of pure array
			start = System.nanoTime();
			size = entsArray.length;
			for(int i=0; i<size;i++) {
				entsArray[i].setName("teste");
			}
			elapsed = System.nanoTime() - start;
			avg3+=elapsed;

		}
		avg1 /= numberOfTests;
		avg2 /= numberOfTests;
		avg3 /= numberOfTests;
		avg1 /= Timer.MILLISECOND;
		avg2 /= Timer.MILLISECOND;
		avg3 /= Timer.MILLISECOND;
		
		System.out.println("for each:\t "+avg1);
		System.out.println("C-type for:\t "+avg2);
		System.out.println("Pure array:\t "+avg3);
	}
}
