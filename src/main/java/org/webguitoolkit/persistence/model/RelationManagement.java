package org.webguitoolkit.persistence.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation indicates a bidirectional relation
 * 
 * That means the relation is automatically managed on the assigned value's site, 
 * that means that if a object is added the relation of the added object is 
 * maintained by aspects and the Relatable class.
 * 
 * So you don't have to add the one2Many functions, just add the RelationManagement 
 * annotation and set the the thisField and the relationField.
 * 
 * <pre>
 *   [at]RelationManagement( thisSide="myMessages", relationSide="parentMessage")
 *   public void removeMyMessage( PersistentObject myMessage ) {
 * 		myMessages.remove( myMessage );
 *   }
 *   
 *   [at]RelationManagement( thisSide="parentMessage", relationSide="myMessages")
 *   public void setParentMessage(PersistentObject parentMessage) {
 *   	this.parentMessage = parentMessage;
 *   }
 * </pre>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface RelationManagement{

	public enum Mode{ MANUEL, AUTO }
	
	/**
	 * @return the variable name on the calling side
	 */
	public String thisSide();

	/**
	 * @return the variable name on the related objects side
	 */
	public String relationSide();

	/**
	 * @return the mode when the relation management is executed if not set execute always
	 */
	public Mode mode() default Mode.AUTO;
}
