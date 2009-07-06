/* Generated By:JJTree: Do not edit this line. ASTPostIncrementNode.java */

package org.bbop.expression.parser;

import org.bbop.expression.ExpressionException;
import org.bbop.expression.JexlContext;
import org.bbop.expression.util.Coercion;

import org.apache.log4j.*;

public class ASTPostIncrementNode extends SimpleNode {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(ASTPostIncrementNode.class);
  public ASTPostIncrementNode(int id) {
    super(id);
  }

  public ASTPostIncrementNode(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  /** {@inheritDoc} */
  @Override
public Object value(JexlContext context) throws Exception {
      // left should be the variable (reference) to assign to
      SimpleNode left = (SimpleNode) jjtGetChild(0);
      Integer val = null;
      try {
    	  val = Coercion.coerceInteger(left.value(context));
      } catch (Exception ex) {
    	  throw new ParseException("Can't use increment operator on non-integer");
      }
      
      ASTReference reference = (ASTReference) jjtGetChild(0);
      left = (SimpleNode) reference.jjtGetChild(0);
      if (left instanceof ASTIdentifier) {
          String identifier = ((ASTIdentifier) left)
                  .getIdentifierString();
          
          try {
        	  context.setVariable(identifier, new Integer(val.intValue()+1));
          } catch (ExpressionException ex) {
        	  ex.decorateException(this);
          }
      }
      
      return val;
  }
}
