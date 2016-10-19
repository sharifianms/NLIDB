package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import model.NLParser;
import model.Node;
import model.ParseTree;
import model.ParseTreeNodeMapper;
import model.ParseTreeStructureAdjuster;
import model.QueryTreeTranslator;
import model.SQLQuery;
import model.SchemaGraph;
import model.WordNet;
import ui.UserView;


/**
 * The controller between model and view.
 * @author keping
 */
public class Controller {
	private Connection connection = null;
	private String input;
	private ParseTreeStructureAdjuster adjuster;
	private QueryTreeTranslator translator;
	private SchemaGraph schema;
	private NLParser parser;
	private ParseTree parseTree;
	private UserView view;
	private ParseTreeNodeMapper mapper;
	private WordNet wordNet;
	private boolean inMappingProcess = false;
	private boolean processing = false;
	
	/**
	 * Initialize the Controller.
	 */
	public Controller(UserView view) {
		this.view = view;
		startConnection();
		
		try { wordNet = new WordNet();
		} catch (Exception e) { e.printStackTrace(); }
		parser     = new NLParser(); // initialize parser, takes some time
		adjuster   = new ParseTreeStructureAdjuster(this);
		translator = new QueryTreeTranslator();
		
		System.out.println("Controller initialized.");
	}
	
	/**
	 * ONLY FOR TESTING. An empty constructor.
	 */
	public Controller() {
		
	}
	
	/**
	 * Start connection with the database and read schema graph
	 */
	public void startConnection() {
		// TODO
		try { Class.forName("org.postgresql.Driver"); } 
		catch (ClassNotFoundException e1) { }
		
		System.out.println("PostgreSQL JDBC Driver Registered!");

		try {
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/dblp", "dblpuser", "dblpuser");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connection successful!");
		
		try {
			schema = new SchemaGraph(connection);
			view.setDisplay("Database Schema:\n\n"+schema.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean isProcessing() { return processing; }
	public String getInput() { return input; }
	public void setInput(String input) { this.input = input; }
	
	
	public void nodesMappingStart() {
		inMappingProcess = true;
		mapper = new ParseTreeNodeMapper(parseTree, schema, wordNet);
		if (mapper.hasNextChoices()) {
			List<Node> choices = mapper.nextChoices();
			view.setDisplay("Mapping nodes: \n"+parseTree.sentenceToString());
			int index = choices.get(0).getIndex();
			view.appendDisplay("\nindex: "+index+"\n");
			view.setChoices(FXCollections.observableArrayList(choices));
			// Here waiting for the button to call chooseNode
		} else {
			finishNodesMapping();
		}
	}
	
	public void chooseNode() {
		if (inMappingProcess) {
			mapper.chooseNode(view.getChoice());
			if (mapper.hasNextChoices()) {
				List<Node> choices = mapper.nextChoices();
				view.setDisplay("Mapping nodes: \n"+parseTree.sentenceToString());
				int index = choices.get(0).getIndex();
				view.appendDisplay("\nindex: "+index+"\n");
				view.setChoices(FXCollections.observableArrayList(choices));
			} else {
				finishNodesMapping();
			}
		}
	}
	
	private void finishNodesMapping() {
		parseTree = mapper.getMappedTree();
		view.setDisplay("Nodes mapped.\n"+parseTree.nodesToString());
		inMappingProcess = false;
	}
	
	/**
	 * Close connection with the database.
	 */
	public void closeConnection() {
		try {
			if (connection != null) { connection.close(); }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Connection closed.");
	}
	/**
	 * Get user input from the view.
	 * @param s
	 * @return
	 */
	public String getUserInput(String s) {
		// TODO: get user input from UserView
		return s;
	}
	/**
	 * Process natural language and return an sql query.
	 * @param nl
	 * @return
	 */
	public void processNaturalLanguage() {
		processing = true;
		parseTree = new ParseTree(input, parser);
		nodesMappingStart();
	}
	
	public SQLQuery getQuery() {
		// TODO
		SQLQuery query;
		query = new SQLQuery(parseTree.sentenceToString()+" to sql query...");
		return query;
	}
	
	/**
	 * Show an sql query in view.
	 * @param query
	 */
	public void viewQuery(SQLQuery query) {
		// TODO
	}
	
	
	//0---- Methods for interactive communication ----
	public void showNodes(ArrayList<ParseTree> trees) {
		// TODO: show possible nodes to user
	}
	public ParseTree getUserChoiceNode() {
		// TODO
		return new ParseTree("User's choice tree nodes", parser);
	}
	public void showStructures(ArrayList<ParseTree> trees) {
		// TODO
	}
	public ParseTree getUserChoiceStructure() {
		// TODO
		return new ParseTree("User's choice tree structure", parser);
	}
	//0-----------------------------------------------
	
}
