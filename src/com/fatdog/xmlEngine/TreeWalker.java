 
package com.fatdog.xmlEngine;

import com.fatdog.xmlEngine.exceptions.InvalidQueryException;
import com.fatdog.xmlEngine.exceptions.CategorizedInvalidQueryException;

import com.fatdog.xmlEngine.javacc.Filter;
import com.fatdog.xmlEngine.javacc.Node;
import com.fatdog.xmlEngine.javacc.RelPath;
import com.fatdog.xmlEngine.javacc.SimpleNode;
import com.fatdog.xmlEngine.javacc.XQueryParserTreeConstants;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Hashtable;
import java.util.StringTokenizer;
	
public class TreeWalker implements XQueryParserTreeConstants
{
	Map<String, Map<String, List<String>>> mtm;
	Map<String, List<String>> pctable;
	public String[] fileList={"1","2","3","3","4"};
	public String tableName="test";
	public String startRow;
	public String stopRow;
	
	
	boolean	m_preserveXmlSpace = false;	// @@@@@ false BY DEFAULT @@@@@
	
	SimpleNode			m_queryRoot;
 
    IndexManager    	m_indexer;
    FandO           	m_fando;
    Variables			m_vars;
    
    QueryDocumentTree	m_enclosedTagConstructors;	// ctored tags and emptyTags in enclosed content get instantiated here
    QueryDocumentTree	m_queryDocTree;				// before being added to the final 'destination' here
    
    Stack				m_filterLhsStack;	// available inside predicate to subordinate query nodes
    										// that need it (they can check their getHasContext() method)
    
    StringBuffer		m_contentSB;	// used to accumulate text for ctor'ed text node
    
    IntList		m_orderSpecList;		// accumulated evaled orderSpec corresponding to each returns eval
    IntList		m_returnCounts;			// how many items per each returns eval. needed to sort properly in DocItems
    
    Stack		m_orderSpecListStack;
    Stack		m_returnCountsStack;
    
    Stack		m_isEnclosedExprStack;
    
	Stack		m_tagTreeDestinationStack;	// 2nd variation of above stack
    
	Hashtable	m_stringsHash		= new Hashtable();
	int			m_nextString		= 0;
	
	Hashtable	m_currNamespaceBindings	= new Hashtable();
	
	ArrayList	gDoubleList		= new ArrayList();
	ArrayList	gFloatList		= new ArrayList();
	ArrayList	gDecimalList	= new ArrayList();
    
    // used to determine if final boundary whitespace exists in constructed element
    
    public final static boolean AT_LEAF = true; // .. ( parent:: ) call at leaf
    
    final static boolean    IS_ATTR         = true;
    final static boolean    IS_PREDICATE    = true;
    final static boolean    DEBUG           = true;
    
    public final static int	ELEM_NODES			= 0;	// called by 
    public final static int TEXT_NODES			= 1;
	public final static int ATTR_NODES			= 2;	// DocItems.textNodesContainingWord()	
	
    final static int		TOTAL_NODE_TYPES	= 3;
    
    final static int		FOR_INDEX			= 0;
    final static int		LET_INDEX			= -1;
    
    //public TreeWalker( IndexManager indexer )
    //---------------------------------------
    public TreeWalker()
    {
        //m_indexer   = indexer;
        
        m_fando     = new FandO( this, ! DEBUG );
        m_vars		= new Variables( this );
        
        m_queryDocTree	= new QueryDocumentTree( this, 100 /*initial entries*/, com.fatdog.xmlEngine.XQEngine.AGGRESSIVE );
		m_enclosedTagConstructors	= new QueryDocumentTree( this, 10,  com.fatdog.xmlEngine.XQEngine.AGGRESSIVE );
         
		m_filterLhsStack = new Stack();
		
		m_orderSpecListStack 	= new Stack();
		m_returnCountsStack		= new Stack();
		
		m_isEnclosedExprStack	= new Stack();
		m_isEnclosedExprStack.push( new Boolean(false));  // by definition, first instantiated tag is not enclosed
		
		m_tagTreeDestinationStack	= new Stack();
		m_tagTreeDestinationStack.push( m_queryDocTree ); // by definition first tag instantiated is not enclosed
    }
    public TreeWalker(String tableName, Map<String, List<String>> pctable,String startRow,String stopRow){
    	//this.mtm = omtm;
		//fileList = (String[])fileName.toArray(new String[0]);
		this.tableName=tableName;
		this.pctable=pctable;
		this.startRow=startRow;
		this.stopRow=stopRow;
		m_vars		= new Variables( this );
    }
    public IndexManager getIndexer()    			{ return m_indexer; }
    //------------------------------
    public QueryDocumentTree getQueryDocumentTree()	{ return m_queryDocTree; }
    //---------------------------------------------
    
    public ResultList walk( SimpleNode root ) throws InvalidQueryException
    //-------------------------------
    {
        SimpleNode next = (SimpleNode) root.jjtGetChild( 0 );
        
        if ( next == null )
        	throw new IllegalArgumentException( "\nTreeWalker.walk(): Could not form valid query tree (is null)" );
     
     	int numChildren = root.jjtGetNumChildren();
     	
     	for ( int child = 0; child < numChildren - 1; child ++ )
     	{
			evalPrologDeclaration( (SimpleNode) root.jjtGetChild( child ) );
     	}
     	
     	next = (SimpleNode) root.jjtGetChild( numChildren - 1 );
        
		// not entirely satisfactory method of not allowing top-level non-rooted
		// access to entire collection. (eg what about parens?)
        
        switch( next.id() )
        {
			case JJTQNAME :     
			case JJTATTRIBUTE : 
			case JJTTEXTTEST : 
			case JJTNODETEST :      
				
				ResultList results = new ResultList( this );
				results.setAST( root );
				return results;
			//case JJTFILTER :        
			//case JJTRELPATH :		
        }
        
        ResultList results = eval( next );
        results.setAST( root );
        
        return results;
    }
    
    void evalPrologDeclaration( SimpleNode node ) throws InvalidQueryException
    //-------------------------------------------
    {
    	switch( node.id() )
    	{
			case JJTXMLSPACEPRESERVE :	m_preserveXmlSpace = true ; 
										break;
			case JJTXMLSPACESTRIP :		m_preserveXmlSpace = false; 
										break;	
            
			case JJTNAMESPACEDECL :		
			
				if (true )	//以前是m_indexer.getEngine().getUseLexicalPrefixes()
				{
					throw new InvalidQueryException( 
						"\nCannot declare a namespace if the UseLexicalPrefixes option is on." );
				}
				
				String namespaceUri	= node.getText();						
				int colonLoc		= namespaceUri.indexOf( ":" );
				
				String prefix 	= namespaceUri.substring( 0, colonLoc );
				String url		= namespaceUri.substring( colonLoc + 2, namespaceUri.length() - 1 );
				
				addNamespaceBinding( prefix, url );

				//System.out.println( "namespace decl : " + prefix + ":" + url );
				break;
							
			case JJTDEFAULTNAMESPACEDECL :
				
				if ( true )//以前是 m_indexer.getEngine().getUseLexicalPrefixes()	
				{
					throw new InvalidQueryException( 
						"\nCannot declare a default namespace if the UseLexicalPrefixes option is on." );
				}
				
				namespaceUri = node.getText();
				url = namespaceUri.substring( 1, namespaceUri.length() - 1 );
				
				System.out.println( "Default namespace URL = " + url );
				break;
			
			default :
			
				throw new InvalidQueryException( "\nTreeWalker: Unknown prolog argument : " + node );
    	}
    }
    
    void addNamespaceBinding( String prefix, String url ) { m_currNamespaceBindings.put( prefix, url ); }
    //---------------------------------------------------   
    
    // we've encountered a QName prefix in a query. look up the prefix and see what uri it corresponds
    // to from its original 'declare namespace' declaration. then look up the uri and get its index, 
    // which is what we'll search the NodeTree for.

    public String currNamespaceBinding( String prefix ) { return (String) m_currNamespaceBindings.get( prefix ); }
    //-------------------------------------------------

    boolean isChildOp( SimpleNode node )
    //----------------------------------
    {
    	return node.id() == JJTRELPATH && node.getText().equals( "/" );
    }
	
    ResultList eval( SimpleNode node ) throws InvalidQueryException
    //--------------------------------
    {
        SimpleNode lhs =  (SimpleNode) node.jjtGetChild(0);
        SimpleNode rhs =  (SimpleNode) node.jjtGetChild(1);
        
        ResultList results = new ResultList( this );
        
        // hasContext turned off by some nodes (eg slashRoot, slashSlashRoot, flwor, constructors ),
        // turned on by filter
        
        if ( lhs != null )	lhs.setHasContext( node.getHasContext() );
        if ( rhs != null ) 	rhs.setHasContext( node.getHasContext() );    

        switch( node.id() )
        {				
            case JJTQNAME :     	int parentId = ((SimpleNode) node.jjtGetParent()).id();
            
            						// NOTE that parens is not really a sufficient test
            						// might be easier to look at what *is* allowed as QName parent ???
            						
            						if ( parentId != JJTRELPATH	&& parentId != JJTSLASHROOT 
            													&& parentId != JJTPARENS 
            													&& parentId != JJTSLASHSLASHROOT 
            													&& parentId != JJTFILTER
            													&& parentId != JJTGENERALCOMP )
            							return results;
            						else
            							return results.newLeafNodeLists( NodeTree.ELEM, node.getText() );
            
            case JJTATTRIBUTE : 	// an early attempt to limit invalid output. I need to think this thru more thoroughly
            						/*
            						parentId = ((SimpleNode) node.jjtGetParent()).id();
									
									if ( parentId != JJTFILTER	&& parentId != JJTSLASHROOT 
																&& parentId != JJTSLASHSLASHROOT )
										return results;
									else
									*/
            							return results.newLeafNodeLists( NodeTree.ATTR, lhs.getText() );
            
            case JJTTEXTTEST :  	return results.newLeafNodeLists( NodeTree.TEXT, null );
            case JJTNODETEST :  	return results.newLeafNodeLists( NodeTree.ALL_NODES, null );
            
            case JJTAND :			return and( lhs, rhs );
            
            case JJTOR :			return or(lhs,rhs);
            
            case JJTFILTER :    	return filter( lhs, rhs );         
       		
			case JJTRELPATH :   	return node.getText().equals( "/" ) ?
	
											child( lhs, rhs, node ) :
											descendant( lhs, rhs );    
            
            case JJTSLASHROOT : 	lhs.setHasContext( false );
            
            						return ( lhs.id() == JJTQNAME ) ?
	                                
	                                        results.newNamedNodesAtRoot( lhs.getText() ) :
	                                        eval( lhs ).nodesAtRoot();
            
            case JJTSLASHSLASHROOT :      
            						lhs.setHasContext( false );
            						       
                                	return eval( lhs );

			case JJTPARENS:			return ( lhs == null ) ?
			
											results :
											eval( lhs );      
            
            case JJTFUNCTIONCALL :                                  
                                	return m_fando.dispatchNamedFunction( node, lhs.getText(), rhs );
            
            case JJTSEQ :       	return eval( lhs ).appendSequence( eval( rhs ) );
            
            case JJTINTEGERLIT: 	return results.newInteger( node.getText() );
            
            case JJTSTRINGLIT :		return results.newString( node.getText() );  
            
            //case JJTFLOATLIT :		return results.newFloat(node.getText());
			
            case JJTDOUBLELIT :		return results.newDouble( node.getText() );
            
            // 17mar05: NOTA: we're currently treating decimals as doubles to simplify code
            
			case JJTDECIMALLIT :	return results.newDecimal( new BigDecimal( node.getText()));
            
            case JJTFLWOR :			lhs.setHasContext( false );
            						rhs.setHasContext( false );
            						
            						return flwor( lhs, rhs );
            
            case JJTVARIABLE :		return m_vars.evalVariableValue( lhs.getText() );
            
            case JJTGENERALCOMP :	return generalComparison( node.getText(), lhs, rhs );
            
            case JJTVALUECOMP :		throw new InvalidQueryException(
            							"\nTreeWalker.eval(): Value comparisons Not Yet Implemented" );
            							
            case JJTORDERCOMP :		return orderComparison( node.getText(), lhs, rhs );
            
            case JJTSOME :			lhs.setHasContext( false );
									rhs.setHasContext( false );
									
            						return some( lhs, rhs );
            
            case JJTEVERY :			throw new InvalidQueryException( 
										"\nTreeWalker.eval(): 'every ...' keyword Not Yet Implemented" );
						
			case JJTTAG :			rhs.setHasContext(false);
									SimpleNode last = (SimpleNode) node.jjtGetChild( node.jjtGetNumChildren() - 1 );
						
									return tag( lhs.getText(), last, rhs, node );
									
			case JJTEMPTYTAG :		if ( rhs != null )
									{
										rhs.setHasContext(false);
									}
									return emptyTag( lhs.getText(), rhs, node );
			
			case JJTCONTENT :		eval( lhs );
			
									if ( rhs != null )
										eval( rhs );
										
									break;
									
			case JJTCHARDATA :		return new ResultList( this ).newString( node.getText() );
									
			case JJTORDERBY :		return eval( lhs ); // return individual orderspec item to RList.sort()
					
			case JJTIFTHENELSE : 	return ifThenElse( lhs, rhs, (SimpleNode) node.jjtGetChild( 2 ));
			
			case JJTADDITIVE :		return node.getText().equals( "+" ) ?
		
											add( lhs, rhs ) :
											subtract( lhs, rhs );    
											
			case JJTMULTIPLICATIVE :		

									char opCode = node.getText().charAt(0);
									
									return ( opCode == '*' ) ? 
											
											multiply( lhs, rhs ) :
											divide( opCode, lhs, rhs );
			
			case JJTUNARY :			return unary( node );
			
            default : 
            	
            	String msg = "\nTreeWalker.eval():";
            	          	
        		throw new InvalidQueryException(  node.id() == JJTPARENT ?
	
						msg + " '../element' Not Yet Implemented" :
						msg + " unknown AST nodetype " + node.id() + " [" + jjtNodeName[ node.id() ] + "]" );             
        }
        
        return null;	// we can get here eg from JJTCONTENT
    }

    int[] singleAtomic( SimpleNode operand ) throws InvalidQueryException
    //--------------------------------------
    {
    	ResultList results = eval( operand ).atomize();
    	
    	if ( results.getNumValidItems() == 0 )
    	{
    		return null;
    	}
    	
    	if ( results.getNumValidItems() > 1 )
        	
    			throw new CategorizedInvalidQueryException(
    							
    				"XP0006", 
    				"TreeWalker.operandCheck(): [Type Error]: Arithmetic operand must evaluate to a single item" );
    				
    	return results.subscript(1).valueType( 0 );
    }
    
    Integer operandCheck( SimpleNode operand ) throws InvalidQueryException
    //----------------------------------------
    {
    	ResultList results = eval( operand ).atomize();
    	
    	if ( results.getNumValidItems() == 0 )
    	{
    		return null;
    	}
    	
    	if (results.getNumValidItems() > 1 )
    	
			throw new CategorizedInvalidQueryException(
							
				"XP0006", 
				"TreeWalker.operandCheck(): [Type Error]: Arithmetic operand must evaluate to a single item" );
				
		int[] item = results.subscript(1).valueType( 0 );
		
		int integer = 0;
		
		int doubleVal;
		
		if ( DocItems.isType( item, DocItems.STRING ))
		{
			
		}
		
		if ( results.isAtomic( item )) // as opposed to node type
		{
			try 
			{ 
				integer = results.castAsInteger( item );
			}
			catch( NumberFormatException nfe )
			{
				throw new CategorizedInvalidQueryException(
							
					"XP0021", 
					"TreeWalker.add(): [Dynamic Error]: Operand cannot be cast to integer" );
			}
		}
		
		return new Integer( integer );
    }
    
    ResultList unary( SimpleNode operator ) throws InvalidQueryException
    //------------------------------------
    {
    	int[] operand = singleAtomic( (SimpleNode) operator.jjtGetChild( 0 ));
    	
    //	Integer operand = operandCheck( (SimpleNode) operator.jjtGetChild( 0 ) );
    	
    	if ( operand == null )
    	
    		return new ResultList( this );
    		
		boolean minus = operator.getText().equals( "-" );

		switch( operand[ 1 ] )
		{
			case DocItems.INT :
				if ( minus )
					operand[ 0 ] = - operand[ 0 ];
				
				return new ResultList( this, operand );
				
			case DocItems. DOUBLE :
				if ( minus )
				{
					double doubleVal = ( (Double)gDoubleList.get( operand[0] ) ).doubleValue();

					return new ResultList(this).newDouble( -doubleVal );
				}
				
			case DocItems. DECIMAL :
				if ( minus )
				{
					double doubleVal = ( (BigDecimal)gDecimalList.get( operand[0] )).doubleValue();
					
					return new ResultList(this).newDecimal( new BigDecimal( -doubleVal ) );
				}
				
			default :
				
				throw new IllegalArgumentException( "BAD ARGUMENT " );
		}
		
	
					  
	//	return new ResultList(this).newInteger( minus? -operand.intValue(): operand.intValue() );
    }
    
    int preMathConversions( int[] lhs, int[] rhs, String operationName ) throws InvalidQueryException
    //------------------------------------------------------------------
    {	
    	if ( DocItems.isString( lhs ) || DocItems.isString( rhs ) )
    			throw new CategorizedInvalidQueryException(
					"XPOOO6", 
					"TreeWalker.add(): String value cannot be used in " + operationName );
    	
    	if ( DocItems.isUntypedAtomic( lhs ))
    		lhs = DocItems.attemptCastAsDouble( this, lhs );
    	
    	if ( DocItems.isUntypedAtomic( rhs ))
    		rhs = DocItems.attemptCastAsDouble( this, rhs );
    	
    	// throws if not one of 4 main numeric types
    	return convertToCommonNumericType( lhs, rhs );
    }
    
    ResultList add( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //----------------------------------------------
    {
    	int[] lhsOp = singleAtomic( lhs ); 	// if double, added to gDoubleList
    	int[] rhsOp = singleAtomic( rhs );
    	
    	if ( lhsOp == null || rhsOp == null ) 
    		return new ResultList( this );
    	
    	int commonType = preMathConversions( lhsOp, rhsOp, "addition operation" );
    	
    	switch( commonType )
		{
    		case DocItems. DOUBLE :		return addCommonTypes( DocItems. DOUBLE, lhsOp, rhsOp );
    		case DocItems. FLOAT :		return addCommonTypes( DocItems. FLOAT, lhsOp, rhsOp );
    		case DocItems. DECIMAL :	return addCommonTypes( DocItems. DECIMAL, lhsOp, rhsOp );
    		case DocItems. INT :		return addCommonTypes( DocItems. INT, lhsOp, rhsOp );
    		
    		default : return null;
		}
     }
    
    ResultList subtract( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //---------------------------------------------------
    {
    	int[] lhsOp = singleAtomic( lhs ); 	// if double, added to gDoubleList
    	int[] rhsOp = singleAtomic( rhs );
    	
    	if ( lhsOp == null || rhsOp == null ) 
    		return new ResultList( this );
    	
    	int commonType = preMathConversions( lhsOp, rhsOp, "subtraction operation" );
    	
    	switch( commonType )
		{
    		case DocItems. DOUBLE :		return subtractCommonTypes( DocItems. DOUBLE, lhsOp, rhsOp );
    		case DocItems. FLOAT :		return subtractCommonTypes( DocItems. FLOAT, lhsOp, rhsOp );
    		case DocItems. DECIMAL :	return subtractCommonTypes( DocItems. DECIMAL, lhsOp, rhsOp );
    		case DocItems. INT :		return subtractCommonTypes( DocItems. INT, lhsOp, rhsOp );
    		
    		default : return null;
		}
     }
    
    ResultList multiply( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //---------------------------------------------------
	{
    	int[] lhsOp = singleAtomic( lhs ); 	// if double, added to gDoubleList
    	int[] rhsOp = singleAtomic( rhs );
    	
    	if ( lhsOp == null || rhsOp == null ) 
    		return new ResultList( this );
    	
    	int commonType = preMathConversions( lhsOp, rhsOp, "multiplication operation" );
    	
    	switch( commonType )
		{
    		case DocItems. DOUBLE :		return multiplyCommonTypes( DocItems. DOUBLE, lhsOp, rhsOp );
    		case DocItems. FLOAT :		return multiplyCommonTypes( DocItems. FLOAT, lhsOp, rhsOp );
    		case DocItems. DECIMAL :	return multiplyCommonTypes( DocItems. DECIMAL, lhsOp, rhsOp );
    		case DocItems. INT :		return multiplyCommonTypes( DocItems. INT, lhsOp, rhsOp );
    		
    		default :  return null; 
		}
	}
    
    ResultList divide( char opCode, SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //--------------------------------------------------------------
    {
    	int[] lhsOp = singleAtomic( lhs ); 	// if double, added to gDoubleList
    	int[] rhsOp = singleAtomic( rhs );
    	
    	if ( lhsOp == null || rhsOp == null ) 
    		return new ResultList( this );
    	
    	int commonType = preMathConversions( lhsOp, rhsOp, "division operation" );
    	
    	switch( commonType )
		{
    		case DocItems. DOUBLE :		return divideCommonTypes( opCode, DocItems. DOUBLE, lhsOp, rhsOp );
    		case DocItems. FLOAT :		return divideCommonTypes( opCode, DocItems. FLOAT, lhsOp, rhsOp );
    		case DocItems. DECIMAL :	return divideCommonTypes( opCode, DocItems. DECIMAL, lhsOp, rhsOp );
    		case DocItems. INT :		return divideCommonTypes( opCode, DocItems. INT, lhsOp, rhsOp );
    		
    		default : return null;
		}
     }

    ResultList addCommonTypes( int opType, int[] opA, int[] opB ) throws InvalidQueryException
    //-----------------------------------------------------------
    {
    	ResultList result = new ResultList(this);
    	
    	switch( opType )
		{
    		case DocItems. DOUBLE :	
    			
    			return result.newDouble( 
								getDouble( opA[0] ).doubleValue() 
							+ 	getDouble( opB[0] ).doubleValue() );
    		
    		case DocItems. FLOAT :
    				
    			return result.newFloat(
								getFloat( opA[ 0 ]).floatValue()
							+	getFloat( opB[ 0 ]).floatValue() );
    		
    		case DocItems. DECIMAL :
    				
    			return result.newDecimal(
								getDecimal(opA[0]).add( getDecimal( opB[0] ) ) );
    		
    		case DocItems.INT :
    				
    			return result.newInteger( opA[0] + opB[ 0 ] );
    		
    		default : return null;
    			//throw new InvalidQueryException( "TreeWalkers.add(): Invalid numeric operand type" );
		}
    }
    
    ResultList subtractCommonTypes( int opType, int[] opA, int[] opB ) throws InvalidQueryException
    //----------------------------------------------------------------
    {
    	ResultList result = new ResultList(this);
    	
    	switch( opType )
		{
    		case DocItems. DOUBLE :	
    			
    			return result.newDouble( 
								getDouble( opA[0] ).doubleValue() 
							- 	getDouble( opB[0] ).doubleValue() );
    		
    		case DocItems. FLOAT :
    				
    			return result.newFloat(
								getFloat( opA[ 0 ]).floatValue()
							-	getFloat( opB[ 0 ]).floatValue() );
    		
    		case DocItems. DECIMAL :
    				
    			return result.newDecimal(
								getDecimal(opA[0]).subtract( getDecimal( opB[0] ) ) );
    		
    		case DocItems.INT :
    				
    			return result.newInteger( opA[0] - opB[ 0 ] );
    		
    		default : return null;
    			//throw new InvalidQueryException( "TreeWalkers.add(): Invalid numeric operand type" );
		}
    }
    
    ResultList multiplyCommonTypes( int opType, int[] opA, int[] opB ) throws InvalidQueryException
    //----------------------------------------------------------------
    {
    	ResultList result = new ResultList(this);

    	switch( opType )
		{
    		case DocItems. DOUBLE :	

				return result.newDouble(	getDouble(opA[0]).doubleValue() 
										* 	getDouble(opB[0]).doubleValue() );	
				
			case DocItems. FLOAT :
				
    			float aFloat = getFloat(opA[0]).floatValue();
				float bFloat = getFloat(opB[0]).floatValue();
				
				return result.newFloat(	getFloat(opA[0]).floatValue() 
									* 	getFloat(opB[0]).floatValue() );  
				
			case DocItems. DECIMAL :
				
				BigDecimal aDec = getDecimal(opA[0]);
				return result.newDecimal( aDec.multiply( getDecimal(opB[0]) ) );
				
			case DocItems. INT :
				
    			return result.newInteger( opA[0] * opB[0] );
			
			default : return null; // never get here
		}
    }
    
    void throwDivideByZero() throws CategorizedInvalidQueryException
	//----------------------
	{
		throw new CategorizedInvalidQueryException( "FOAR0001", "Divide by zero error" );
	}
    
    ResultList divideCommonTypes( char opCode, int opType, int[] opA, int[] opB ) throws InvalidQueryException
    //---------------------------------------------------------------------------
    {
    	ResultList result = new ResultList(this);
    	
    	switch( opType )
		{
    		case DocItems. DOUBLE :	
    			
    			double aDouble = getDouble(opA[0]).doubleValue();
    			double bDouble = getDouble(opB[0]).doubleValue();
    			
    			if ( bDouble == 0 ) throwDivideByZero();
    			
    			switch( opCode )
				{	
    				case '/' :	return result.newDouble( aDouble / bDouble );   				
    				case 'i' :	return result.newInteger( (int)( aDouble / bDouble ) );   				
    				case 'm' :	return result.newDouble( aDouble % bDouble );			
    				// never get here (never say never?)
				}
    		
    		case DocItems. FLOAT :
    			
    			float aFloat = getFloat(opA[0]).floatValue();
				float bFloat = getFloat(opB[0]).floatValue();
    				
				if ( bFloat == 0 ) throwDivideByZero();
				
    			switch( opCode )
				{				
    				case '/' :	return result.newFloat( aFloat / bFloat );   				
    				case 'i' :	return result.newInteger( (int)( aFloat / bFloat ) );  				
    				case 'm' :	return result.newFloat( aFloat % bFloat );
				}
				  		
    		case DocItems. DECIMAL :
    			
    			BigDecimal aDec = getDecimal(opA[0]);
    			BigDecimal bDec = getDecimal(opB[0]);
    			
    			if ( bDec.doubleValue() == 0 ) throwDivideByZero();
    				
    			switch( opCode )
				{
    				case '/' :	return result.newDecimal( aDec.divide( bDec, BigDecimal.ROUND_HALF_EVEN ) );
    				case 'i' : 	BigDecimal quotient = aDec.divide( bDec, BigDecimal.ROUND_HALF_EVEN );
    							return result.newInteger( (int) quotient.doubleValue());
    				case 'm' :	return result.newDecimal( new BigDecimal( aDec.doubleValue() % bDec.doubleValue() ) );			
				}
    		
    		case DocItems.INT :
    				
    			int aInt = opA[ 0 ];
    			int bInt = opB[ 0 ];
    			
    			if ( bInt == 0 ) throwDivideByZero();
    			
    			switch( opCode )
				{
    				case '/' :	return result.newDouble( (double)aInt / (double)bInt );
    				case 'i' :	return result.newInteger( aInt / bInt );
    				case 'm' :  return result.newInteger( aInt % bInt );			
				}
    		
    		default : return null;
    			//throw new InvalidQueryException( "TreeWalkers.add(): Invalid numeric operand type" );
		}
    }
    
    // NOTA: operand type evaluation order is important
    
    int convertToCommonNumericType( int[] opA, int[] opB ) throws InvalidQueryException
    //-----------------------------------------------------
    {
    	if ( DocItems. isDouble( opA ) || DocItems. isDouble( opB ) )
		{
			asDouble( opA );
			asDouble( opB );			return DocItems. DOUBLE;
		}
    	
    	if ( DocItems. isFloat( opA ) || DocItems. isFloat( opB ))
    	{
    		asFloat( opA );
    		asFloat( opB );				return DocItems. FLOAT;
    	}
    	
		if ( DocItems. isDecimal( opA ) || DocItems. isDecimal( opB ))
		{
			asDecimal( opA );
			asDecimal( opB );			return DocItems.DECIMAL;
		}
		
		if ( DocItems.isInteger( opA ) || DocItems.isInteger( opB ))
		{
			asInteger( opA );
			asInteger( opB );			return DocItems.INT;
		}
		
    	throw new InvalidQueryException( "TreeWalker.convertToCommonNumericType(): Invalid numeric type" );
    }
    
    void asDouble( int[] item ) throws CategorizedInvalidQueryException
    //------------------------
    {
    	switch( item[ 1 ] )
		{
    		case DocItems. INT :
    			
        			item[ 0 ] = newDoubleToList( item[ 0 ] );
        			item[ 1 ] = DocItems. DOUBLE;						break;
        			
    		case DocItems.DOUBLE :										break;
    		
    		case DocItems. DECIMAL :
    			
    				BigDecimal bigDec = (BigDecimal) gDecimalList.get( item[ 0 ]);
    				double doubleVal = bigDec.doubleValue();
    		
    				item[ 0 ] = newDoubleToList( doubleVal );
    				item[ 1 ] = DocItems. DOUBLE;						break;
    		
    		case DocItems. UNTYPED_ATOMIC :
    			
    				doubleVal = DocItems.castAsDouble( item );
    		
    				item[ 0 ] = newDoubleToList( doubleVal );
    				item[ 1 ] = DocItems. DOUBLE;						break;
    		
    		default : 
    			
    				throw new CategorizedInvalidQueryException(
	    				"XP0006",
						"TreeWalker.add(): Operand was not promotable to double" );
		}
    }
    
    void asFloat( int[] item ) throws InvalidQueryException
	//------------------------
	{
    	switch( item[ 1 ] )
		{
    		case DocItems. FLOAT :			break;	// no-op
		}
    	
    	throw new InvalidQueryException( "TreeWalker.asInteger(): Invalid Integer value" );  
	}
    
    void asDecimal( int[] item ) throws InvalidQueryException
	//--------------------------
	{
    	switch( item[ 1 ] )
		{
    		case DocItems. DECIMAL :		break;	// no-op

    		case DocItems. INT :
    			
        			item[ 0 ] = newDecimalToList( new BigDecimal( new Integer( item[ 0 ] ).toString() ) );
        			item[ 1 ] = DocItems. DECIMAL;
        			
        									break;
    		default :
    	
    			throw new InvalidQueryException( "TreeWalker.asDecimal(): Invalid decimal value" );  	
		}
	}
    
    void asInteger( int[] item ) throws InvalidQueryException
	//--------------------------
	{
    	switch( item[ 1 ] )
		{
    		case DocItems. INT :			break;	// no-op
    		
    		default :
    			
    			throw new InvalidQueryException( "TreeWalker.asInteger(): Invalid integer value" );
		}  	
	}
    
 	public ResultList ifThenElse(	SimpleNode ifClause, 
									SimpleNode thenClause, 
									SimpleNode elseClause ) throws InvalidQueryException
//---------------------------------------------------------
{
		ResultList boolResult = eval( ifClause );
		
		if ( ! boolResult.isSingleBoolean() )
		
			throw new InvalidQueryException( 
				"\nTreeWalker.ifThenElse(): IF clause does not evaluate to a boolean result" );
		
		if ( boolResult.getSingleBooleanValue() )
		{
			return eval( thenClause );
		}
		
		return eval( elseClause );
}
	
    ResultList and( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //----------------------------------------------
    {
        ResultList boolResults = new ResultList(this);
        
		if ( lhs.getHasContext() || rhs.getHasContext() )
		{
			System.out.println( "has context!" );
		}
		
		//取集合的交集
		return boolResults.intersection(eval(lhs), eval(rhs));
		
       	//return boolResults.newBoolean( eval(lhs).booleanValue() && eval(rhs).booleanValue() );
    }
    
    ResultList or( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    {
    	ResultList boolResults = new ResultList(this);
        
		if ( lhs.getHasContext() || rhs.getHasContext() )
		{
			System.out.println( "has context!" );
		}
		
		//取集合的并集
		return boolResults.union(eval(lhs), eval(rhs));
    }

    StringBuffer attributeContent( SimpleNode contents ) throws InvalidQueryException
    //--------------------------------------------------
    {
		SimpleNode lhs =  (SimpleNode) contents.jjtGetChild(0);
		SimpleNode rhs =  (SimpleNode) contents.jjtGetChild(1);
    	
		StringBuffer sb = new StringBuffer( string_value_attr( lhs ));
		
    	if ( rhs != null )
    		sb.append( attributeContent( rhs ));  		
    	
    	return sb;
    }
	
	String string_value_attr( SimpleNode item ) throws InvalidQueryException
	//-----------------------------------------
	{
		String contents = item.getText();
		
		switch( item.id() )
		{
			case JJTINTEGERLIT	:	
			case JJTSTRINGLIT	:	return contents;
			
			case JJTCHARDATA	:	return normalize( contents );
			
			case JJTSEQ			:	SimpleNode lhs = (SimpleNode)item.jjtGetChild(0);
									SimpleNode rhs = (SimpleNode)item.jjtGetChild(1);
									
									StringBuffer result = new StringBuffer( string_value_attr( lhs ));

									if ( result.length() != 0 )
									
										result.append( " " );
										
									result.append( string_value_attr( rhs ));								
										
									return result.toString();
									
			case JJTENCLOSEDEXPR :
									
			//default :	// we can't eval the query-tree item statically, so we do it dynamically
			
				ResultList encloseExprResults = eval( (SimpleNode)item.jjtGetChild(0));
				
				if ( encloseExprResults.getNumValidItems() == 0 )
				
					return "";
			
				return encloseExprResults.asString( true ); // add space between items
				
			default :
			
					throw new InvalidQueryException( 
									"\nTreeWalker.string_value_attr(): Unknown item type : " + item.id() );
		}
	}
	
	// NOTA: fn:normalize-space() does following, as well as stripping leading and trailing whitespace
	// QUESTION:	move this to somewhere else?? (eg as static in Fando maybe ???)

	String normalize( String str )
	//----------------------------
	{
		StringTokenizer tokenizer = new StringTokenizer( str );
		StringBuffer sb = new StringBuffer();
		
		while( tokenizer.hasMoreTokens() )
		{
			sb.append( tokenizer.nextToken() );
			if ( tokenizer.hasMoreTokens() )
				sb.append( ' ' );
		}
		
		return sb.toString();
	}
	
	boolean isTerminalWhitespace( String text )
	//-----------------------------------------
	{
		int priorNodeType = m_queryDocTree.getType( m_queryDocTree.getLastAddedNode() );
		
		boolean hasContent = ! ( priorNodeType == NodeTree. ELEM || priorNodeType == NodeTree. ATTR );
		
		return isWhitespace( text ) && ( hasContent || m_contentSB.length() > 0 );
	}
	
	// 9oct03 order of adding eval'ed contents to QDocTree works ok if separate eval blocks {}{}
	// w/in a tag ctor, since first block gets eval'ed and added to tree before the 2nd. Problem
	// arises 
	
	/*

<results> { for $b in doc("bib.xml")/bib/book return <result>{ $b/title } { $b/author } </result> } </results>
	
<a>{//@*,<b>123{<c/>}</b>}</a>
	
		
<a>1234{<b>567<c/></b>}<d/></a>
			
	*/
	
	void elementContent( SimpleNode contents ) throws InvalidQueryException
	//----------------------------------------
	{	
		SimpleNode lhs =  (SimpleNode) contents.jjtGetChild(0);
		SimpleNode rhs =  (SimpleNode) contents.jjtGetChild(1);
		
		QueryDocumentTree currTagTreeDestination =  (QueryDocumentTree) m_tagTreeDestinationStack.peek();
			
		ResultList results = new ResultList(this);
		
		if ( lhs.id() == JJTTAG || lhs.id() == JJTEMPTYTAG )
		{	
			// add the ctor'ed element to the proper tree (if using multiple trees) as a side effect of evaluation
			// we mark the element as QueryDocumentTree.ENCLOSED_CTOR if inside {} eval braces
			
			m_isEnclosedExprStack.push( new Boolean(false) );
			
			results = eval( lhs ); 			
			
			m_isEnclosedExprStack.pop();
		}
		
		else if ( lhs.id() == JJTCHARDATA )
		{		
			boolean terminalWhitespace = false;
			
			// if we're at the terminal boundary and not preserving whitespace			
			if ( rhs == null && !m_preserveXmlSpace )
			{			
				// and the prior content was any other sort of content (ie not the tag itself)		
				
				if ( ((SimpleNode) contents.jjtGetParent()).id() == JJTCONTENT )
				
					// and our chardata really is whitespace					
					if ( isWhitespace( lhs.getText()) )
					{
						terminalWhitespace = true;
					}
			}
				
			if ( ! terminalWhitespace )
			
				currTagTreeDestination.addTextNode( lhs.getText() );		
		}	
		
		else if ( lhs.id() == JJTENCLOSEDEXPR )
		{	
			int reservedNodeForEnclosedExprResults = currTagTreeDestination.reserveNodeForEnclosedResults();
			
			m_isEnclosedExprStack.push( new Boolean(true) );
			
			results = eval( (SimpleNode) lhs.jjtGetChild(0 ) );
			
			results.amalgamateAdjacentAtomics();
			
			currTagTreeDestination.updateReservedResults( reservedNodeForEnclosedExprResults, results );
			
			m_isEnclosedExprStack.pop();	
		}
		
		if ( rhs != null )
		{
			elementContent( rhs );
		}
	}
	
 //  <a> 1{//editor}{2}<b/>3 </a>
 
 /*
 
<results>{<result_1>{"1"}</result_1>,<result_2>{"22"}</result_2>}</results>

<r>{for $i in //book return <book numAuthors="{count($i/author)}">{for $t in $i/title return $t/text()}</book>}</r>
 
 */
	ResultList tag( String startTagName, SimpleNode endTag, SimpleNode rhs, SimpleNode parent ) throws InvalidQueryException
	//-----------------------------------------------------------------------------------------
	{
		// currently not using m_tagTreeDestinationStack -- it's always m_queryDocTree --
		// but allows easy switch to alternative, 2-tree mechanism if we want to use that structure
		
		QueryDocumentTree destinationTree = (QueryDocumentTree) m_tagTreeDestinationStack.peek();
		
		if ( ! startTagName.equals( endTag.getText() ) )
			throw new InvalidQueryException( "\nTreeWalker.tag(): start and end tag names must match" );
		
		boolean isEnclosed = ((Boolean) m_isEnclosedExprStack.peek()).booleanValue();
		
		destinationTree.startElement( null, startTagName, isEnclosed );	
		
		int nextQueryNode = 1;

		if ( rhs.id() == JJTATTRIBUTE )
		{
			nextQueryNode = parseAttribute( rhs, nextQueryNode, parent );
		}

		SimpleNode nextNode = (SimpleNode) parent.jjtGetChild( nextQueryNode );
		
		if ( nextNode.id() == JJTCONTENT )
		{			
			SimpleNode contents	= (SimpleNode) nextNode.jjtGetChild(0);
			
			// initial boundary whitespace ? skip if called for
			
			if ( !m_preserveXmlSpace && contents.id() == JJTCHARDATA && isWhitespace( contents.getText() ) )
			{
				SimpleNode nextContent = (SimpleNode) nextNode.jjtGetChild(1);
				if ( nextContent != null )
				{
					nextNode = nextContent;
				}				
			}		
		
			elementContent( nextNode );
		}
		
		int elemId = destinationTree.endElement();
		
		int elemParent = destinationTree.getParent( elemId );
		
		// good time to copy from tree to tree (when we do it)
		
		if ( elemParent == -1 )
		{
			// doing it backwards rite now just to get it going
			
			// destinationTree.CopyNodesToQueryDocTree( m_enclosedTagConstructors, elemId );
			//System.out.println( "@");
		}
		
		return destinationTree.newConstructedElement( elemId );
	}
	
	boolean isWhitespace( String str )
	//--------------------------------
	{
		StringTokenizer tokenizer = new StringTokenizer( str );
		
		return !tokenizer.hasMoreTokens();
	}
	     
	ResultList emptyTag( String tagName, SimpleNode rhs, SimpleNode parent ) throws InvalidQueryException
	//----------------------------------------------------------------------
	{   
		QueryDocumentTree destinationTree = (QueryDocumentTree) m_tagTreeDestinationStack.peek();
		
		boolean isEnclosed = ((Boolean) m_isEnclosedExprStack.peek()).booleanValue();
		
		destinationTree.startElement( null, tagName, isEnclosed );		
		
		if ( rhs != null && rhs.id() == JJTATTRIBUTE )
		{
			parseAttribute( rhs, 1, parent );
		}	
	
		int elemNodeId = destinationTree.endElement();

		return destinationTree.newConstructedElement( elemNodeId );
	}
/*
 
for $b in doc("bib.xml")/bib/book
where $b/publisher = "Addison-Wesley" and $b/@year > 1991
return
<book year="{ $b/@year }">
     { $b/title }
</book>
    
 */
 	int parseAttribute( SimpleNode attribute, int childIx, SimpleNode parent ) throws InvalidQueryException
	//------------------------------------------------------------------------
	{
		String attrName 	= ((SimpleNode) attribute.jjtGetChild(0)).getText();
		
		String attrValue	= attributeContent( (SimpleNode) attribute.jjtGetChild(1) ).toString();
		
		m_queryDocTree.addAttribute( attrName, attrValue );

		SimpleNode nextNode = (SimpleNode) parent.jjtGetChild( ++ childIx );
		
		if ( nextNode != null && nextNode.id() == JJTATTRIBUTE )
		{
			return parseAttribute( nextNode, childIx, parent );
		}
		
		return childIx;
	}
	
    ResultList some( SimpleNode inClause, SimpleNode satisfies ) throws InvalidQueryException
    //----------------------------------------------------------
    {
    	SimpleNode inChild = (SimpleNode) inClause.jjtGetChild( 0 );
    	if ( inChild.id() == JJTQIN )
    	           
			throw new InvalidQueryException( 
				"\nTreeWalker.some(): multiple 'in' clauses Not Yet Implemented for 'some' keyword" );	         	
	                
    	// only a single 'in' ...
    	
		SimpleNode variable = inChild;		
		SimpleNode value = (SimpleNode) inClause.jjtGetChild(1);
		
		ResultList evalResults = eval( value );
		
		String varName = ((SimpleNode) variable.jjtGetChild( 0 )).getText();			
		int variableId = m_vars.newVariable( varName, evalResults, FOR_INDEX );
		
		for ( int i = 0; i < evalResults.getNumTotalItems(); i++ )
		{
			ResultList testExpression = eval( satisfies );
			
			boolean efb = testExpression.effectiveBooleanValue();
			
			if ( efb == true )
				return new ResultList( this ).newBoolean( true );
			
			m_vars.advanceForIndex( variableId );
		}

    	return new ResultList( this ).newBoolean( false );
    }

/* 
let $i := 1, $j := 2 let $k := 3 return 4 

for $b in doc("bib.xml")/bib/book, $t in $b/title, $a in $b/author return 1

*/
	ResultList flwor( SimpleNode forLet, SimpleNode returnClause ) throws InvalidQueryException
	//------------------------------------------------------------
	{
		boolean hasOrderSpec = getOrderSpec( returnClause ) != null;
		
		if ( hasOrderSpec )
		{
			m_orderSpecListStack.push( new IntList(2) );
			m_returnCountsStack.push( new IntList(1) );
		}
		
		ResultList results = forLet( forLet, returnClause );
		
		if ( hasOrderSpec )
		{
			results.sort( (IntList) m_orderSpecListStack.peek(), (IntList) m_returnCountsStack.peek() );	
		
			m_orderSpecListStack.pop();
			m_returnCountsStack.pop();
		}

		return results;
	}
	
	int getOrderSpecChildId( SimpleNode returnClause )
	//------------------------------------------------
	{
		int childIx = -1;	// there isn't one
		
		int numChildren = returnClause.jjtGetNumChildren();
		
		if ( numChildren > 1 )	
		{
			if ( ( (SimpleNode)returnClause.jjtGetChild( numChildren - 2)).id() == JJTORDERBY )
			{
				childIx = numChildren - 2;
			}
		}
		
		return childIx;
	}
	
	SimpleNode getOrderSpec( SimpleNode returnClause )
	//------------------------------------------------
	{
		int numChildren =  returnClause.jjtGetNumChildren();
		
		if ( numChildren == 1 ) 
		{
			return null;
		}
		
		SimpleNode possibleOrderSpec = (SimpleNode) returnClause.jjtGetChild( numChildren - 2 );
		
		return possibleOrderSpec.id() == JJTORDERBY ? possibleOrderSpec : null;
	}
	
	public String getOrderSpecVariableName( SimpleNode orderSpec )
	//------------------------------------------------------------
	{
		SimpleNode node = (SimpleNode) orderSpec.jjtGetChild(0);
		
		while( node.id() != JJTVARIABLE && node.jjtGetNumChildren() > 0 )
		{
			SimpleNode lhs = (SimpleNode) node.jjtGetChild( 0 );
			SimpleNode rhs = (SimpleNode) node.jjtGetChild(1 );
			
			node = lhs.id() == JJTVARIABLE ? lhs : rhs;	
		}
		
		if ( node.id() != JJTVARIABLE )
		
			throw new IllegalArgumentException( "\nResultList.processOrderSpec(): No variable in order spec!" );
			
		return ((SimpleNode)node.jjtGetChild( 0 )).getText();
	}
	
/*
 for $i in 1, $j in 2 for $k in 3 return 4

for $b in /bib/book 
where $b/publisher = "Addison-Wesley" 
return $b

*/
	ResultList forLet( SimpleNode forLetNode, SimpleNode returnClause ) throws InvalidQueryException
	//-----------------------------------------------------------------
	{	
		SimpleNode clause 		= (SimpleNode) forLetNode.jjtGetChild(0 );
		SimpleNode nextForLet	= (SimpleNode) forLetNode.jjtGetChild( 1 );

		if ( clause.id() == JJTFOR )
		{
			return forClause( clause, (SimpleNode)clause.jjtGetChild(1), nextForLet, returnClause );
		}
		else
			return letGroup( clause, nextForLet, returnClause );
	}
	
	ResultList forClause( SimpleNode forNode, SimpleNode nextFor, SimpleNode nextForLet, SimpleNode returnClause ) throws InvalidQueryException
	//------------------------------------------------------------------------------------------------------------
	{
		if ( ((SimpleNode)forNode.jjtGetChild(0)).id() == JJTFOR )
		{
			forNode = (SimpleNode) forNode.jjtGetChild(0);
		}
		
		SimpleNode variable = (SimpleNode) forNode.jjtGetChild(0);
		SimpleNode value = (SimpleNode) forNode.jjtGetChild(1);
		
		ResultList forLoopVariableValue = eval( value );
		
		String varName = ((SimpleNode) variable.jjtGetChild( 0 )).getText();			
		int variableId = m_vars.newVariable( varName, forLoopVariableValue, FOR_INDEX );
		
		SimpleNode returnClauseLHS =  (SimpleNode) returnClause.jjtGetChild(0);
		
		ResultList results = new ResultList( this );
		
		//for( int i = 0; i < forLoopVariableValue.getNumValidItems(); i++ )
		//{
			ResultList rhsItems;
			
			if ( nextFor.id() == JJTFOR )
			{
				results = forClause( nextFor, (SimpleNode) nextFor.jjtGetChild(1), nextForLet, returnClause );
			}
			else if ( nextForLet != null )
			{			
				results = forLet( nextForLet, returnClause );
			}
			else
			{		
				results = returns( returnClause );
			}
			
			//m_vars.advanceForIndex( variableId );
			//m_vars.unbindFollowing( variableId );
			
			//if ( rhsItems.getNumValidItems() == 0 )
				//continue;
				
			//results.append( rhsItems );
		//}
		
		return results;
	}
	
	
	ResultList letGroup( SimpleNode letNode, SimpleNode nextForLet, SimpleNode returnClause ) throws InvalidQueryException
	//---------------------------------------------------------------------------------------
	{		
		SimpleNode letChild_1 = (SimpleNode) letNode.jjtGetChild(0);
	
		if ( letChild_1.id() == JJTVARIABLE )
		{
			letClause( letNode );
			
			if ( nextForLet != null )
			
				return forLet( nextForLet, returnClause );
		}
		else
		{
			letClause( letChild_1  );
			
			SimpleNode nextChild = (SimpleNode) letNode.jjtGetChild( 1 );
			
			if ( ((SimpleNode) nextChild.jjtGetChild( 0 )) .id() == JJTVARIABLE )
			{
				letClause( nextChild );
				
				if ( nextForLet != null )
				
					return forLet( nextForLet, returnClause );
			}
			else
			
				return letGroup( nextChild, nextForLet, returnClause );
		}
	
	return returns( returnClause );	
		/*
		SimpleNode letChild_1 = (SimpleNode) letNode.jjtGetChild(0);
		
		if ( letChild_1.id() == JJTVARIABLE )
		{
			letClause( letNode );
		}
		else
		{
			letClause( letChild_1  );
			
			SimpleNode nextChild = (SimpleNode) letNode.jjtGetChild( 1 );
			
			if ( ((SimpleNode) nextChild.jjtGetChild( 0 )) .id() == JJTVARIABLE )
			
				letClause( nextChild );
				
			else
			
				return letGroup( nextChild, null, returnClause );
		}
		
		return returns( returnClause );
		*/	
	}
	
 	void letClause( SimpleNode letNode ) throws InvalidQueryException
	//----------------------------------
	{
		SimpleNode variable = (SimpleNode) letNode.jjtGetChild(0);
		SimpleNode value = (SimpleNode) letNode.jjtGetChild(1);
		
		String varName = ((SimpleNode) variable.jjtGetChild( 0 )).getText();			
		m_vars.newVariable( varName, eval( value ), LET_INDEX );
	}
	
	boolean isAbsoluteRoot( SimpleNode node )
	//---------------------------------------
	{
		int nodeId = node.id();
		
		if ( nodeId == JJTSLASHROOT )
			return true;
		else if ( nodeId == JJTSLASHSLASHROOT )
			return true;
		else if ( nodeId == JJTQNAME && node.getText().equals( "doc"))
			return true;
		else if ( nodeId == JJTVARIABLE )
			return true;
		else if ( nodeId == JJTTAG || nodeId == JJTEMPTYTAG )
			return true;
			
		return false;
	}
	
	boolean isAbsolutePathRoot( SimpleNode node )
	//-------------------------------------------
	{
		if ( isAbsoluteRoot( node ))
		{
			return true;
		}
			
		int id = node.id();
		
		if ( id == JJTPARENS || id == JJTSEQ )
		{
			return isAbsolutePathRoot( (SimpleNode) node.jjtGetChild( 0 ) );
		}
		
		if ( ( id == JJTRELPATH || id == JJTFILTER ) && isAbsoluteRoot( (SimpleNode) node.jjtGetChild( 0 ) ))
		{
			return true;
		}
		
		return false;
	}

	boolean where( SimpleNode whereArgument ) throws InvalidQueryException
	//---------------------------------------
	{
		 ResultList survives = eval( whereArgument );
		 String varTmp=survives.var;
		 ResultList parent=null;
		 for(int i=0;i<m_vars.m_name.length;i++){
			 if(varTmp.equals(m_vars.m_name[i])){
				 //估计要按照变量的类型（for let）进行不同操作,let直接getReal();for则替换。
				 parent=m_vars.m_value[i];
				 if(m_vars.m_forLetIndex[i]==LET_INDEX){
					 System.out.println("是let参数进行getValues操作");
					 try {
						parent.getValues();
						return true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 //Modded by kanchuanqi 2013.3.19 For 'let' clause
				 parent.columns.clear();
				 parent.file.clear();
				 //parent.columns=survives.columns;
				 //parent.file=survives.file;
				 parent.columns.addAll(survives.columns);
				 parent.file.addAll(survives.file);
				 //parent.resultMap=survives.resultMap;
				 System.out.println("替换变量："+varTmp);
				 break;
			 }
		 }
		 //根据survives的xpath，取出所有的变量，判断是哪个变量，并当变量的类型为For时利用survives结果集进行更新。
		/* for(int i=0;i<m_vars.m_name.length;i++){
			 if(survives.xpath.startsWith(m_vars.m_value[i].xpath)&&m_vars.m_forLetIndex[i]>=0){
				 int size=survives.columns.size();
				 String[] tmp=m_vars.m_value[i].xpath.split("/");
				 int deep=tmp.length;
				 deep=deep*4-3;
				 String oldTmp="";
				 String newTmp="";
				 for(int j=0;j<size;j++){
					 newTmp=survives.columns.get(j).substring(0, deep-1);
					 if(oldTmp.equals(newTmp))
						 continue;
					 oldTmp=newTmp;
					 m_vars.m_value[i].columns.add(newTmp);
					 m_vars.m_value[i].file.add(survives.file.get(j));
				 }
				 return true;
			 }
		 }*/
		 return true;	 // NOTA: throws IllegalArgument if it isn't a single boolean
	}	
	
	ResultList returns( SimpleNode returnClause ) throws InvalidQueryException
	//-------------------------------------------
	{		
		SimpleNode returnExpr = (SimpleNode) returnClause.jjtGetChild( returnClause.jjtGetNumChildren() - 1 );

	/*	needs more thought ...
		if ( !isAbsolutePathRoot( returnExpr ))	// weed out things like 'bare' QName and Attributes
			returnResults = new ResultList( this );
		else
			returnResults = eval( returnExpr );
	*/		
		
		boolean doReturn = true;
				
		if (  ((SimpleNode) returnClause.jjtGetChild( 0 )).id() == JJTWHERE )
		{
			System.out.println("进入where");
			SimpleNode whereClause = (SimpleNode) returnClause.jjtGetChild( 0 ).jjtGetChild(0);
			doReturn = where( whereClause );
		}
		else{
			
			ResultList returnResults = eval( returnExpr );
			try {
				returnResults.getValues();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return returnResults;
		}
		
		
		
		if ( doReturn ) 
		{
			ResultList returnResults = eval( returnExpr );
			
			int orderSpecChildIx = getOrderSpecChildId( returnClause );
			
			if ( orderSpecChildIx >= 0 )
			{
				SimpleNode orderSpec = (SimpleNode) returnClause.jjtGetChild( orderSpecChildIx );
				
				appendToOrderSpecResultList( orderSpec );
			
				IntList returnCounts = (IntList) m_returnCountsStack.pop() ;
				
				returnCounts.addRef_1( returnResults.getNumValidItems() );
				
				m_returnCountsStack.push( returnCounts );
			}
			
			returnResults.getReal();
			/*try {
				returnResults.getValues();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			System.out.println("Return xpath="+returnResults.xpath);
			
			
			
			return returnResults;
		}
		
		return new ResultList( this ); // empty result list
		
	}
	
	// 3oct BUG:
	// for $i in //book order by $i/title return for $j in $i/author order by $j/first return $j
	
	void appendToOrderSpecResultList( SimpleNode orderSpec ) throws InvalidQueryException
	//------------------------------------------------------
	{
		ResultList orderSpecResults = eval( orderSpec );
			
		int numOSpecResults = orderSpecResults.getNumValidItems();
			
		int[] valueType;
		
		if ( numOSpecResults == 0 )
		{
			valueType = new int[ 2 ];
			valueType[ 1 ] = DocItems. NULL_ORDERSPEC;
		}
		else if ( numOSpecResults == 1 )
		
			valueType = orderSpecResults.valueType( 0 );
		
		else
		{
			throw new CategorizedInvalidQueryException( 
					
					"XP006", 
					"ResultList.sort(): Evaluation of an orderSpec returned multiple values" );
		}
		
		IntList orderSpecs = (IntList) m_orderSpecListStack.pop();
		
		orderSpecs.addRef_Both( valueType );
		
		m_orderSpecListStack.push( orderSpecs );
	}
	
    ResultList child( SimpleNode lhs, SimpleNode rhs, SimpleNode childOp ) throws InvalidQueryException
    //--------------------------------------------------------------------
    {
        int lhsId   = lhs.id();
        int rhsId   = rhs.id();
        
        if ( lhsId == JJTTAG || lhsId == JJTEMPTYTAG )
        
        	throw new InvalidQueryException( 
				"\nTreeWalker.child(): you currently cannot navigate into constructed nodes");
           
		if ( rhsId == JJTPARENT )
			return parentOf( lhs );
			
		// this is a kludge and not totally correct
		// we'll simplify and fix this next revision or shortly thereafter
		
        if ( ! isChildOp(  (SimpleNode) childOp.jjtGetParent() ))
        {       	
        	if ( isChildOp( rhs ) )
        	{
        		SimpleNode grandChildOp = (SimpleNode) rhs.jjtGetChild(1);
        		if ( grandChildOp != null )
        		{
        			boolean hasParentOp =  
        					grandChildOp.id() == JJTPARENT ||
        				( 	isChildOp( grandChildOp ) && ( (SimpleNode) grandChildOp.jjtGetChild( 0 )).id() == JJTPARENT );
        				    				
        			if ( hasParentOp )  
        			{
        				ResultList newList = constructedFilterTree( lhs, rhs, childOp );
        				return newList;
        			}
        		}
        	}
        }   
        
        if ( rhsId == JJTATTRIBUTE )
            return attribute( lhs, ((SimpleNode) rhs.jjtGetChild(0)).getText() );
            
		if ( rhsId == JJTDOT )
			return eval( lhs );
			
        if ( rhsId == JJTQNAME )
            return namedChild( lhs, rhs.getText(), NodeTree. ELEM );
            
		// TO-DO review if we want to call newNamedParentOfNamedChild w/ text() and node() tests
    	// (eg, called namedChild() above instead)
    	
        if ( rhsId == JJTNODETEST )
			return eval( lhs ).namedChildOfParent( null, NodeTree. ALL_NODES, !IS_PREDICATE );
			//return namedChild( lhs, null, NodeTree. ALL_NODES );
		
		if ( rhsId == JJTTEXTTEST )
			return eval( lhs ).namedChildOfParent( null, NodeTree. TEXT, !IS_PREDICATE );
		       
/*		commented out 26jan04 as not in use in junit test files
		if ( isChildOp( rhs ) && ( (SimpleNode) rhs.jjtGetChild( 0 )).id() == JJTPARENT )
		{
			System.out.println( "!!" );
			
			return eval( rhs ).evalParent( parentOf( lhs ), false );
		}
*/
			
        // we're into eval( rhs ) territory now
        
        if ( lhsId == JJTQNAME )
        {
        	ResultList tempR = eval( rhs ).namedParentOfEvaledChild( lhs.getText() );
        	return tempR;
        }
                  
        ResultList parentResults = eval( lhs );
        
        if ( parentResults.containsAtomics() )
        
        	throw new CategorizedInvalidQueryException( 
				
						"XP0019", 
						"TreeWalker.child(): result of this evaluation was not a node sequence" );
        	
        // new signature 30sept03
        
        return new ResultList( this ).evalParent( parentResults, eval(rhs), false );
  
        //return eval( rhs ).evalParent( eval( lhs ), false );    
    }
      
	ResultList namedChild( SimpleNode parent, String child, int type ) throws InvalidQueryException
	//--------------------------------------------------------------
	{   
		// TO-DO: parameterize newNamedParentOfNamedChild() on type
    	//System.out.println("进入namedChild()");
		if ( parent.id() == JJTQNAME )
		{
			ResultList tempR = new ResultList( this ).newNamedParentOfNamedChild( parent.getText(), child, !IS_ATTR, !IS_PREDICATE );                           
			return tempR;
		}
          
		ResultList parentResults = eval( parent );
		//System.out.println("hello "+parentResults.var+parentResults.columns);
		if ( parentResults.containsAtomics() )
			
			throw new CategorizedInvalidQueryException( 
				
						"XP0019", 
						"TreeWalker.namedChild(): result of this evaluation was not a node sequence" ); 
				                    
		 return parentResults.namedChildOfParent( child, type, !IS_PREDICATE );
	} 
     
	// we've encountered a subtree of form Q1/Q2/.. or Q1/Q2/../Q3
	// we want to rebuild and evaluate it either as Q1[ Q2 ] or Q1[ Q2 ]/Q3 respectively
   
	public ResultList constructedFilterTree( SimpleNode Q1, SimpleNode childOp, SimpleNode parent ) 
																		throws InvalidQueryException
	//----------------------------------------------------------------------------------------------
	{
		SimpleNode Q2 = (SimpleNode) childOp.jjtGetChild( 0 );
		
		Filter filter = new Filter( JJTFILTER );	
		filter.jjtAddChild( Q1, 0 );
		filter.jjtAddChild( Q2, 1 );
				
		// is of form Q1/Q2/.. --> Q1[ Q2 ]
		
		SimpleNode nextOp = (SimpleNode) childOp.jjtGetChild(1);
		if ( nextOp.id() == JJTPARENT )
		{
			return eval( filter );
		}
		else if ( nextOp.jjtGetNumChildren() > 0 )	// it's not QName eg; is an operator
		{
			SimpleNode nextLhs = (SimpleNode) nextOp.jjtGetChild( 0 );
			SimpleNode nextRhs = (SimpleNode) nextOp.jjtGetChild( 1 );
			
			if ( nextLhs.id() == JJTPARENT )
			{
				//System.out.println( "yes!" );
				//System.out.println();
				
				if ( nextOp.id() == JJTRELPATH )
				{
					RelPath relPath = new RelPath( JJTRELPATH );
					relPath.setText( nextOp.getText() ); // child or descendant
					
					relPath.jjtAddChild( filter, 0 );
					relPath.jjtAddChild( nextRhs, 1 );
					
					relPath.jjtSetParent( parent );
					
					return eval( relPath );
				}
			}
		}

		return null;
	}

    ResultList attribute( SimpleNode lhs, String attr ) throws InvalidQueryException
    //-------------------------------------------------
    {
        ResultList results = new ResultList( this );
        
        if ( lhs.id() == JJTQNAME )
        {
            return results.newNamedParentOfNamedChild( lhs.getText(), attr, IS_ATTR, !IS_PREDICATE );
        }
        
        return eval( lhs ).namedChildOfParent( attr, NodeTree.ATTR, !IS_PREDICATE );
    }
    
    ResultList parentOf( SimpleNode child ) throws InvalidQueryException
    //-------------------------------------
    {
        ResultList results = new ResultList( this );
        
        switch( child.id() )
        {
            case JJTQNAME :     return results.newParentOf( NodeTree.ELEM, child.getText() );
            case JJTATTRIBUTE : return results.newParentOf( NodeTree.ATTR, ((SimpleNode) child.jjtGetChild(0)).getText() );
            case JJTTEXTTEST :  return results.newParentOf( NodeTree.TEXT, null );
            case JJTNODETEST :  return results.newParentOf( NodeTree.ALL_NODES, null );
        }
        
        return eval( child ).parentOf();
    }

    ResultList descendant( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //-----------------------------------------------------
    {
        int lhsId   = lhs.id();
        int rhsId   = rhs.id();
           
		if ( lhsId == JJTTAG || lhsId == JJTEMPTYTAG )
        
			throw new InvalidQueryException( 
				"\nTreeWalker.descendant(): you currently cannot navigate into constructed nodes");
        
		String	name;
        int 	nodeType;
        
        if ( lhsId != JJTQNAME && lhsId != JJTATTRIBUTE )
        {     	
        	// a trivial but nasty bug: if lhs is the doc() function, eval'ing it first will force
        	// indexing *before* we call eval on the rhs. If we embedded the eval(lhs) as
        	// an arg (as done before), the eval( rhs ) would find nill results for that doc -- it hasn't
        	// been indexed yet! -- and we'd get a null ResultList
        	
        	ResultList lhrResults = eval( lhs );
			return eval( rhs ).evalAncestor( lhrResults );	
        }    
   
    	if ( lhsId == JJTQNAME )
    	{
        	nodeType = NodeTree. ELEM ;
        	name = lhs.getText();
    	}
        else
        {
       		nodeType = NodeTree. ATTR;
			name = ((SimpleNode)lhs.jjtGetChild(0)).getText();
        }
                                                                                              
        return eval( rhs ).namedAncestor( name, nodeType );
     }
    
    ResultList newFilterOnNamedPredicate( SimpleNode lhs, String predicate, boolean isAttr ) throws InvalidQueryException
    //----------------------------------------------------------------------
    {
    	if ( lhs.id() == JJTQNAME )
            return new ResultList( this ).newNamedParentOfNamedChild( 
                                        lhs.getText(), predicate, isAttr, IS_PREDICATE );
                                    
        return eval( lhs ).namedChildOfParent( predicate, NodeTree.ELEM, IS_PREDICATE );                 
    }
    
	// pretty brain-dead algorithm right now, but good enough to start out with
	// NOTA: we're only going down the LHS side of any operators we encounter
    
	boolean hasContextDependence( SimpleNode node )
	//---------------------------------------------
	{
		SimpleNode firstChild =  (SimpleNode) node.jjtGetChild(0);
		
		if ( node.id() == JJTSLASHROOT )
			return false;
		else if ( node.id() == JJTSLASHSLASHROOT )
			return false;
		else if ( node.id() == JJTFUNCTIONCALL && firstChild.id() == JJTQNAME && firstChild.getText().equals("doc") )
			return false;
		else if ( node.id() == JJTVARIABLE )
			return false;
			
		else if ( node.id() == JJTQNAME )
			return true;
		else if ( node.id() == JJTATTRIBUTE )
			return true;
		
		return hasContextDependence( firstChild );
	}
	
    ResultList filter( SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
    //-------------------------------------------------
    {
        int lhsId = lhs.id();
        int rhsId = rhs.id();
        
		if ( lhsId == JJTTAG || lhsId == JJTEMPTYTAG )
        
			throw new InvalidQueryException( 
				"\nTreeWalker.filter(): you currently cannot filter on the contents of constructed nodes");
        
        if ( rhsId == JJTINTEGERLIT )
        {
			return subscript( lhs, Integer.parseInt( rhs.getText() ) );
        }
        else if ( rhsId == JJTGENERALCOMP )
        {
        	; // return complexPositional( lhs, rhs );
        }   
        else if ( rhsId == JJTQNAME )
        {
				return newFilterOnNamedPredicate( lhs, rhs.getText(), !IS_ATTR );  
    	}               
        else if ( rhsId == JJTATTRIBUTE )
        {
                return newFilterOnNamedPredicate( lhs, ((SimpleNode)rhs.jjtGetChild(0)).getText(), IS_ATTR );  
        }
        
        
		ResultList parentResults = eval( lhs );
		
		if ( parentResults.getNumValidItems() == 0 )
		{
			return new ResultList( this );
		}
        
		m_filterLhsStack.push( lhs );
		rhs.setHasContext( true );
        
		if ( parentResults.containsAtomics() )
        {
			throw new CategorizedInvalidQueryException(
						
					"XP0019", 
					"TreeWalker.filter(): Currently LHS needs to evaluate to a node sequence" );
        }
		
		ResultList rhsResults = eval( rhs );
		
		ResultList results;
		
		if ( rhsResults.hasUsedContext() )
			results = rhsResults;
		else	
			results = new ResultList( this ).evalParent( parentResults, rhsResults, true );
        
        m_filterLhsStack.pop();
        
        return results;
    }
    
	ResultList generalComparison( String op, SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
	//-----------------------------------------------------------------------
	{
		System.out.println("进行比较函数");
		SimpleNode 	context 		= null;		
		boolean		hasUsedContext 	= false;
		
		if ( lhs.getHasContext() || rhs.getHasContext() )
		{
			context = (SimpleNode) m_filterLhsStack.peek();
		}
		
		// if we're not inside a filter ... (we could be in a 'where', a 'satisfies', or just 'naked')
		
    	if ( context == null )
		{
			return new ResultList( this ).generalCompare( op, eval(lhs), eval(rhs ));
		}
		
		// we're in a filter 
		
		if ( isNamedFunction( lhs, "position" ) )
		{
			return positionalComparison( op, rhs, context );
		}
			
		if ( lhs.id() == JJTDOT )
		{
			lhs = context; hasUsedContext = true;
		}
			
		ResultList results = new ResultList( this ).generalCompareOnLhs( op, eval( lhs ), eval( rhs ));

		results.setHasUsedContext( hasUsedContext );
		
		return results;
	}
	
    ResultList positionalComparison( String op, SimpleNode rhs, SimpleNode context ) throws InvalidQueryException
    //------------------------------------------------------------------------------
    {
		int subscript = -1; // assumes last()
		
		if ( rhs.id() == JJTINTEGERLIT )
		
			subscript = Integer.parseInt( rhs.getText() );
		
		else if ( ! isNamedFunction( rhs, "last" ))

			throw new InvalidQueryException( 
				"\nTreeWalker.positionalComparison(): Currently position() can only be compared against integers or last()" );
							
		ResultList siblings = new ResultList(this);		
		siblings.setHasUsedContext( true ); // tell the containing filter it's already been eval'ed
		
		switch( context.id() )
		{
			case JJTQNAME 		:	siblings.newSiblingLists( NodeTree.ELEM, context.getText() ); break;		
			case JJTTEXTTEST 	:	siblings.newSiblingLists( NodeTree.TEXT, null ); break;
			case JJTNODETEST 	:	siblings.newSiblingLists( NodeTree.ALL_NODES, null ); break;
			
			default :
			
					siblings = eval( context );
					siblings.setHasUsedContext( true );
										
					return siblings.complexBlockPositional( op, subscript );
		}
						
		return siblings.complexPositional( op, subscript );
    }

    ResultList subscript( SimpleNode lhs, int subscript ) throws InvalidQueryException
    //---------------------------------------------------
    {
  		if ( lhs.id() == JJTPARENS | lhs.id() == JJTFUNCTIONCALL )
		{
			return eval(lhs).subscript(subscript);
		}
		
		ResultList results = new ResultList( this );     
		
        switch( lhs.id() )
        {
	        case JJTQNAME    :  return results.newSiblingLists( NodeTree.ELEM, lhs.getText(), subscript );
	        case JJTTEXTTEST :  return results.newSiblingLists( NodeTree.TEXT, null, subscript );
	        case JJTNODETEST :  return results.newSiblingLists( NodeTree.ALL_NODES, null, subscript );
	        
	        default :  
		        
				throw new InvalidQueryException( 
				"\nTreeWalker.subscript(): currently only works on QNames, text(), node(), and parens '()'" );	
        }
    }
    
  /*
   
for $p in doc("report1.xml")//section[section.title = "Procedure"]
where not( some $a in $p//anesthesia satisfies
        $a << ($p//incision)[1] )
return $p
 
  
   */
    
    
	ResultList orderComparison( String op, SimpleNode lhs, SimpleNode rhs ) throws InvalidQueryException
	//---------------------------------------------------------------------
	{
		ResultList lrl = eval( lhs );
		ResultList rrl = eval( rhs );
		
		int lrlValidItems = lrl.getNumValidItems();
		int rrlValidItems = rrl.getNumValidItems();
		
		if ( lrlValidItems == 0 )
			return new ResultList( this );

		if ( rrlValidItems == 0 )
			return new ResultList( this );
		
		if ( lrlValidItems > 1 || rrlValidItems > 1 )	
		
			throw new CategorizedInvalidQueryException(
						
				"XP0006", 
				"TreeWalker.orderComparison(): [Type Error]: One or both operands are returning multiple items" );
				
		int[] lItem = lrl.valueType(0);
		int[] rItem = rrl.valueType(0);
		
		// there's exactly one item each
		
		if ( ! lrl.isNode( lItem ) && ! rrl.isNode( rItem ))
		
			throw new CategorizedInvalidQueryException(
				
				"XP0006", 
				"TreeWalker.orderComparison(): [Type Error]: Both operands must be a single node or empty" );
				
		if ( lrl.headDocument().getId() != rrl.headDocument().getId() )
		{
			if ( op.equals( "<<" ))
				return new ResultList( this ).newBoolean( lrl.headDocument().getId() < rrl.headDocument().getId() );
			else
				return new ResultList( this ).newBoolean( lrl.headDocument().getId() > rrl.headDocument().getId() );
		}
			
		if ( op.equals( "<<"))
			return new ResultList( this ).newBoolean( lrl.getItemValue( lItem) < rrl.getItemValue( rItem ) );
		else
			return new ResultList( this ).newBoolean( lrl.getItemValue( lItem) > rrl.getItemValue( rItem ) );			
	}
    
    // used to characterize the type of our xpath leaf for DocItems.textNodesContainingWord()
    
    public boolean[] getNodeTypesAtLeaf( SimpleNode path )
    //----------------------------------------------------
    {
    	boolean[] types = new boolean[ TOTAL_NODE_TYPES ];
    	
    	evalLeafType( path, types );
    	
    	return types;
    }
    
    void evalLeafType( SimpleNode op, boolean[] types )
    //-------------------------------------------------
    {
		switch( op.id() )
		{
			case JJTSTARTNODE :		evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); return;
			
			case JJTQNAME :     	types[ ELEM_NODES ] = true; return;
			case JJTATTRIBUTE : 	types[ ATTR_NODES ] = true; return;
			case JJTTEXTTEST :  	types[ TEXT_NODES ] = true; return;
			case JJTNODETEST :  	types[ ELEM_NODES ] = true;
									types[ TEXT_NODES ] = true;
									types[ ATTR_NODES ] = true; return;
									
			case JJTINTEGERLIT :	return;	// we don't expect INTEGERLITs -- what to do ??
			case JJTSTRINGLIT :		return; // ditto STRINGLITs -- what to do ??
											// PLUS all the other non xpath leaf types to come ... ??
			
			case JJTFILTER :    	evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); return;			
			case JJTRELPATH :   	evalLeafType( (SimpleNode) op.jjtGetChild( 1 ), types ); return;			
			case JJTSLASHROOT : 	evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); return;
			case JJTSLASHSLASHROOT: evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); return;  		
			case JJTPARENS:			evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); return;
			
			case JJTSEQ :       	evalLeafType( (SimpleNode) op.jjtGetChild( 0 ), types ); 
									evalLeafType( (SimpleNode) op.jjtGetChild( 1 ), types ); return;			
			
			case JJTFUNCTIONCALL :  m_fando.evalFunctionReturnType( op, types ); return;
			
			case JJTPARENT :		Node parent = op.jjtGetParent();			
									if ( parent.jjtGetNumChildren() == 2 && parent.jjtGetChild( 1 ) == op )
										 evalLeafType( (SimpleNode) parent.jjtGetChild( 0 ), types ); return;
			
			default :
			
				throw new IllegalArgumentException( 
					"\nTreeWalker.evalLeafType(): did not understand nodeType at path leaf: " + op.id() );
		}
    }
    
    public int newDoubleToList( String doubleStr )
    //--------------------------------------------
    {
    	Double doubleVal = null;
    	
    	// NOTA: 1.0e0 going in gets emitted as 1.0
    	// by Double.toString(). I'll try to chop the .0 on
    	// the other end (at getDouble() time)
    	
    	try
		{
    		doubleVal = new Double( doubleStr );
		}
    	catch( NumberFormatException nfe ) { /* grammar ensures it's double */ }
    	
    	gDoubleList.add( doubleVal );
    	
    	return gDoubleList.size() - 1;
    }
    
    public int newDoubleToList( double val )
    //-----------------------------------
    {
    	gDoubleList.add( new Double( val ) );
    	
    	return gDoubleList.size() - 1;
    }
    
    public int newFloatToList( float val )
    //------------------------------------
    {
    	gFloatList.add( new Double( val ) );
    	
    	return gFloatList.size() - 1;
    }
    
    public int newDecimalToList( BigDecimal bigDec )
    //----------------------------------------------
    {
    	gDecimalList.add( bigDec );
    	
    	return gDecimalList.size() - 1;
    }
    
	public int newStringToList( String string )
	//-----------------------------------------
	{
		int numStrings = m_stringsHash.size();
		m_stringsHash.put( new Integer( numStrings ), string );
		
		return numStrings;
	}
	
	public Double getDouble( int ix )
	//-------------------------------
	{ 
		return (Double) gDoubleList.get( ix ); 
	}
	
	public Float getFloat( int ix )			{ return (Float) gFloatList.get( ix ); }
	//-----------------------------
	public BigDecimal getDecimal( int ix )	{ return (BigDecimal) gDecimalList.get( ix ); }
	//------------------------------------
	
	public String getStringResult( int stringId )
	//-------------------------------------------
	{  	
		return (String) m_stringsHash.get( new Integer( stringId ));
	}
	
	boolean isNamedFunction( SimpleNode node, String functionName )
	//-------------------------------------------------------------
	{
		return node.id() == JJTFUNCTIONCALL &&  ((SimpleNode)node.jjtGetChild(0)).getText().equals( functionName );
	}
}
