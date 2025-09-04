package cipm.consistency.vsum.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public final class ModelElementsCounter {
	public static class ReferenceCountResult {
		private int containmentReferences;
		private int nonContainmentReferences;
		
		ReferenceCountResult(int containmentReferences, int nonContainmentReferences) {
			this.containmentReferences = containmentReferences;
			this.nonContainmentReferences = nonContainmentReferences;
		}
		
		public int getNumberOfContainmentReferences() {
			return this.containmentReferences;
		}
		
		public int getNumberOfNonContainmentReferences() {
			return this.nonContainmentReferences;
		}
	}
	
	private ModelElementsCounter() {
	}
	
	public static int countModelElements(Resource resource) {
		AtomicInteger counter = new AtomicInteger();
		resource.getAllContents().forEachRemaining(o -> counter.incrementAndGet());
		return counter.get();
	}
	
	public static int countModelElements(EObject eobj) {
		AtomicInteger counter = new AtomicInteger(1);
		eobj.eAllContents().forEachRemaining(o -> counter.incrementAndGet());
		return counter.get();
	}
	
	public static ReferenceCountResult countReferences(EObject eobj) {
		AtomicInteger containCounter = new AtomicInteger();
		AtomicInteger nonContainCounter = new AtomicInteger();
		containCounter.addAndGet(eobj.eContents().size());
		nonContainCounter.addAndGet(eobj.eCrossReferences().size());
		eobj.eAllContents().forEachRemaining(child -> {
			child.eContents().forEach(o -> containCounter.incrementAndGet());
			child.eCrossReferences().forEach(o -> nonContainCounter.incrementAndGet());
		});
		return new ReferenceCountResult(containCounter.get(), nonContainCounter.get());
	}
}
