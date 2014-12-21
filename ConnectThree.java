import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.ArrayList;

public abstract class ConnectThree extends Application {
	/**
	 * The width in columns/cells of the game grid.
	 */
	protected static final int GRID_WIDTH = 4;
	/**
	 * The height in rows/cells of the game grid.
	 */
	protected static final int GRID_HEIGHT = 5;
	/**
	 * The total number of cells in the game grid.
	 */
	protected static final int CELL_COUNT = GRID_WIDTH * GRID_HEIGHT;

	/**
	 * The number of games to run when automating tests.
	 */
	protected static final int AUTOMATE_GAMES = 500;
	
	// GUI SETUP
	
	private Label winnerIcon;
	private ChoiceBox<String> playerControlX, playerControlO;
	private int winCountX, winCountO, drawCount;
	
	private static final String style = ConnectThree.class.getResource("style.css").toExternalForm();
	
	@Override
	public final void start(Stage stage) {
        stage.setTitle("Connect Three");
        
		preNewGames();
		
		BorderPane pane = new BorderPane(); {
			pane.getStyleClass().add("pane");
        	 
        	MenuBar bar = new MenuBar(); {
        		pane.setTop(bar);
        		
        		Menu game = new Menu("_Game"); {
        			bar.getMenus().add(game);
        			
        			MenuItem newgame = new MenuItem("_New Game");
        			newgame.setAccelerator(KeyCombination.keyCombination("SHORTCUT+N"));
        			newgame.setOnAction(new NewGameEvent());
        			game.getItems().add(newgame);
    				
        			MenuItem exit = new MenuItem("E_xit");
        			exit.setAccelerator(KeyCombination.keyCombination("ESC"));
    				exit.setOnAction(new ExitEvent());
    				game.getItems().add(exit);
        		}
        		
        		Menu testing = new Menu("_Testing"); {
        			bar.getMenus().add(testing);

        			MenuItem auto = new MenuItem("_Automate Games");
        			auto.setAccelerator(KeyCombination.keyCombination("SHORTCUT+A"));
        			auto.setOnAction(new AutomateEvent());
        			testing.getItems().add(auto);
        			
        			MenuItem rest = new MenuItem("_Reset History");
        			rest.setAccelerator(KeyCombination.keyCombination("SHORTCUT+R"));
        			rest.setOnAction(new ResetEvent());
        			testing.getItems().add(rest);
        			
        			MenuItem history = new MenuItem("Output _History");
        			history.setAccelerator(KeyCombination.keyCombination("SHORTCUT+H"));
        			history.setOnAction(new HistoryEvent());
        			testing.getItems().add(history);
        		}
        	} 
        	
        	GridPane grid = new GridPane(); {
    			pane.setCenter(grid);
    			pane.getCenter().setStyle("-fx-font-size: 24px;");

    			grid.setPadding(new Insets(15));
    			grid.setHgap(10);
    			grid.setVgap(10);
    			
        		for (int r = 0; r < GRID_HEIGHT; r++) {
        			for (int c = 0; c < GRID_WIDTH; c++) {
        				Button button = new Button();
        				button.setOnMouseClicked(new CellClickEvent(r, c));
        				button.getStyleClass().add("cell-none");
        				button.setMinSize(50, 50);
        				button.setPrefSize(125, 125);
        				button.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        				button.setStyle("-fx-font-size: 400%;");
        				grid.add(button, c, r);
                		GridPane.setHgrow(button, Priority.ALWAYS);
                		GridPane.setVgrow(button, Priority.ALWAYS);
        				cells[r][c] = new Cell(button);
        			}
        		}
        	}
        	
        	HBox status = new HBox(); {
    			pane.setBottom(status);
    			pane.getBottom().setStyle("-fx-font-size: 24px;");
        		
    			status.setPadding(new Insets(0, 15, 15, 15));
    			
    			playerControlX = new ChoiceBox<String>();
    			playerControlX.getItems().addAll(aiList());
				playerControlX.getSelectionModel().selectFirst();
				playerControlX.getStyleClass().addAll("cell-playerx");
				playerControlX.setStyle("-fx-font-size: 100%;");
				playerControlX.setPrefWidth(175);
				playerControlX.setPrefHeight(50);
				playerControlX.getSelectionModel().selectedItemProperty().addListener(new SwitchAiEvent(Player.PLAYERX));
				HBox.setHgrow(playerControlX, Priority.ALWAYS);   
				
				Region spacer1 = new Region();
			    HBox.setHgrow(spacer1, Priority.SOMETIMES);   
			    
    			winnerIcon = new Label("");
    			winnerIcon.setPrefWidth(175);
    			winnerIcon.setPrefHeight(50);
    			winnerIcon.getStyleClass().addAll("cell-hidden");
    			winnerIcon.setStyle("-fx-font-size: 100%;");
    			HBox.setHgrow(winnerIcon, Priority.ALWAYS);   
    			
				Region spacer2 = new Region();
			    HBox.setHgrow(spacer2, Priority.SOMETIMES);   
				
    			playerControlO = new ChoiceBox<String>();
    			playerControlO.getItems().addAll(aiList());
				playerControlO.getSelectionModel().selectFirst();
				playerControlO.getStyleClass().addAll("cell-playero");
				playerControlO.setStyle("-fx-font-size: 100%;");
				playerControlO.setPrefWidth(175);
				playerControlO.setPrefHeight(50);
				playerControlO.getSelectionModel().selectedItemProperty().addListener(new SwitchAiEvent(Player.PLAYERO));
				HBox.setHgrow(playerControlO, Priority.ALWAYS);   
				
				status.getChildren().addAll(
					playerControlX, spacer1, winnerIcon, spacer2, playerControlO
				);
        	}
        } 

		newGame();
		
		Scene scene = new Scene(pane);
		scene.getStylesheets().add(style);
		stage.setScene(scene); 
		stage.setResizable(false);
		stage.setOnShown(new EventHandler<WindowEvent> () {
			@Override
			public void handle(WindowEvent event) {
		        final double reasonable_height = Screen.getPrimary().getVisualBounds().getHeight() * 0.85;
		        if (stage.getHeight() > reasonable_height) {
		        	final double old_height = stage.getHeight();
			        final double hw_ratio = stage.getWidth() / stage.getHeight();
			        stage.setHeight(reasonable_height);
			        stage.setWidth(stage.getHeight() * hw_ratio);
			        final int size = (int) Math.round(24 * (stage.getHeight() / old_height));
			        pane.getCenter().setStyle("-fx-font-size: " + size + "px;");
			        pane.getBottom().setStyle("-fx-font-size: " + size + "px;");
		        }
			}
		});
        stage.show();
	}
	
	// EVENT HANDLERS
	
	private final class NewGameEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			newGame();
		}
	}
	
	private final class ExitEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			Platform.exit();
		}
	}
	
	private final class HistoryEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
	    	double totalGames = winCountX + winCountO + drawCount;
	    	System.out.printf("%d: %d%% to %d%%\n",
	    		(int) totalGames,
	    		(int) (100 * winCountX / totalGames),
	    		(int) (100 * winCountO / totalGames)
	    	);
		}
	}
	
	private final class ResetEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
	    	initializeHistory();
		}
	}
	
	private final class AutomateEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (
				playerControlX.getSelectionModel().getSelectedItem() != "Human" &&
				playerControlO.getSelectionModel().getSelectedItem() != "Human"
			) {
				for (int i = 0; i < AUTOMATE_GAMES; i++) 
					newGame();
			}
		}
	}
	
	private final class SwitchAiEvent implements ChangeListener<String> {
		private Player player;
		
		public SwitchAiEvent(Player player) {
			this.player = player;
		}
		
		@Override public void changed(ObservableValue<? extends String> selected, String oldValue, String newValue) {
			if (newValue != "Human" && turn == player) {
				playAiTurn();
			}
		}
	}
	
	private final String getSelectedAi() {
		if (turn == Player.PLAYERX)
			return playerControlX.getSelectionModel().getSelectedItem();
		else if (turn == Player.PLAYERO)
			return playerControlO.getSelectionModel().getSelectedItem();
		else return "Human";
	}
	
	private final class CellClickEvent implements EventHandler<MouseEvent> {
		@SuppressWarnings("unused")
		private int row, column;
		public CellClickEvent(int row, int column) {
			this.row = row;
			this.column = column;
		}
		
		@Override
		public void handle(MouseEvent e) {
			playColumn(column);
		}
    };
	
	// GAME SETUP
	
	/**
	 * Especially used to register the AI's using
	 * {@link #registerAi}, but may include any other
	 * initialization to be run before the game starts.
	 */
	protected abstract void initialization();
	
	private Cell[][] cells = new Cell[GRID_HEIGHT][GRID_WIDTH];
	private Player turn;
	private int elapsed;
	
	private final void preNewGames() {
		registerAi(new Human());
		registerAi(new Random());
		registerAi(new Naive());
		initialization();
		
		initializeHistory();
	}
	
	private final void initializeHistory() {
		winCountX = 0;
		winCountO = 0;
		drawCount = 0;
	}
	
	private final void newGame() {
		for (int i = 0; i < GRID_HEIGHT; i++)
			for (int j = 0; j < GRID_WIDTH; j++)
				cells[i][j].setPlayerNone();
		
		setTurn(Player.PLAYERX);
		clearWinner();
		
		elapsed = 0;
		
		if (getSelectedAi() != "Human") playAiTurn();
	}
	
	private final void setTurn(Player player) {
		if (player == Player.PLAYERX)
			playerControlX.getStyleClass().remove("cell-fade");
		else if (!playerControlX.getStyleClass().contains("cell-fade"))
			playerControlX.getStyleClass().add("cell-fade");
		
		if (player == Player.PLAYERO)
			playerControlO.getStyleClass().remove("cell-fade");
		else if (!playerControlO.getStyleClass().contains("cell-fade"))
			playerControlO.getStyleClass().add("cell-fade");
		
		turn = player;
	}
	
	private final void clearWinner() {
		winnerIcon.getStyleClass().removeAll("cell-playerx", "cell-playero", "cell-none", "cell-hidden");
		winnerIcon.getStyleClass().add("cell-hidden");
		winnerIcon.setText("");
	}
    
    // CELLS AND STATE
    
    private final class Cell {
    	private Button button;
    	private Player player;
    	
    	public Cell(Button button) {
    		this.button = button;
    		this.player = Player.NONE;
    	}
    	
    	private void setPlayerNone() {
    		player = Player.NONE;
    		button.getStyleClass().clear();
    		button.getStyleClass().add("cell-none");
    		button.setText("");
    	}
    	private boolean isPlayerNone() {
    		return player == Player.NONE;
    	}
    	
    	private void setPlayerX() {
    		player = Player.PLAYERX;
    		button.getStyleClass().clear();
    		button.getStyleClass().add("cell-playerx");
    		button.setText("X");
    	}
    	
    	@SuppressWarnings("unused")
		private boolean isPlayerX() {
    		return player == Player.PLAYERX;
    	}
    	
    	private void setPlayerO() {
    		player = Player.PLAYERO;
    		button.getStyleClass().clear();
    		button.getStyleClass().add("cell-playero");
    		button.setText("O");
    	}
    	
    	@SuppressWarnings("unused")
    	private boolean isPlayerO() {
    		return player == Player.PLAYERO;
    	}
    }
	
    // PLAYING AND WIN DETECTION
    
    /**
     * Represents Player X's turn, Player O's turn, or neither.
     */
    public enum Player{NONE, PLAYERX, PLAYERO};
    
    private final void playAiTurn() {
    	int choice = getAi(turn).getBestColumn(now(), turn);
    	if (choice != -1) playColumn(choice);
    }
    
    private final void playColumn(int column) {
    	int row = top(now(), column);
		if (elapsed < CELL_COUNT && row >= 0) {
			Cell target = cells[row][column];
    		if (target.isPlayerNone()) {
    			elapsed++;
    			
    			if (turn == Player.PLAYERX) {
    				target.setPlayerX();
    				if (elapsed < CELL_COUNT) 
    					setTurn(Player.PLAYERO);
    			}
    			else {
    				target.setPlayerO();
    				if (elapsed < CELL_COUNT) 
    					setTurn(Player.PLAYERX);
    			}
    			
    			Player result = getWinner(now(), row, column);
    			if (result == Player.PLAYERX) {
    				winnerIcon.getStyleClass().remove("cell-hidden");
    				winnerIcon.getStyleClass().add("cell-playerx");
    				winnerIcon.setText("X Wins!");
    				elapsed = CELL_COUNT;
    				winCountX++;
    			}
    			else if (result == Player.PLAYERO) {
    				winnerIcon.getStyleClass().remove("cell-hidden");
    				winnerIcon.getStyleClass().add("cell-playero");
    				winnerIcon.setText("O Wins!");
    				elapsed = CELL_COUNT;
    				winCountO++;
    			}
    			else if (elapsed == CELL_COUNT) {
    				winnerIcon.getStyleClass().remove("cell-hidden");
    				winnerIcon.getStyleClass().add("cell-none");
    				winnerIcon.setText("Draw!");	
    				drawCount++;
    			}
    			
    			if (getSelectedAi() != "Human") playAiTurn();
    		}
		}
    }
    
	/**
	 * Determines the winner of a game state, if any.
	 * @param state Any game state, or the current when called initially.
	 * @param rowHint The row index for the marked cell, if useful.
	 * @param columnHint The column index for the marked cell, if useful.
	 * @return The winning player.
	 */
	protected abstract Player getWinner(Player[][] state, int rowHint, int columnHint);
	

    
    // HELPER ACCESSORS
    
	/**
	 * Retrieves a copy of the current game state.
	 * @return The current state.
	 */
	protected final Player[][] now() {
    	Player[][] state = new Player[GRID_HEIGHT][GRID_WIDTH];
    	for (int r = 0; r < GRID_HEIGHT; r++) {
    		for (int c = 0; c < GRID_WIDTH; c++) {
    			state[r][c] = cells[r][c].player;
    		}
    	}
    	return state;
    }
    
    /**
     * Determines the uppermost empty position in the given column
     * of the given game state, or -1 if the column is full.
     * @param state Any game state.
     * @param column A column index.
     * @return The empty position or -1.
     */
    protected final int top(Player[][] state, int column) {
    	for (int r = GRID_HEIGHT-1; r >= 0; r--)
    		if (column >= 0 && state[r][column] == Player.NONE)
    			return r;
    	return -1;
    }
    
	// ARTIFICIAL INTELLIGENCE

	private final static ArrayList<AI> ais = new ArrayList<>();
	
	private final static ArrayList<String> aiList() {
		ArrayList<String> names = new ArrayList<>();
		for (AI ai: ais)
			names.add(ai.name());
		return names;
	}
	/**
	 * Implements an artificial intelligence.
	 */
	protected abstract class AI {
		public String name() {
			return this.getClass().getSimpleName();
		};
		/**
		 * Determines the recommended best column in which to place the player's mark.
		 * @param state Any game state, or the current when called initially.
		 * @param player The player whose mark is to be placed this turn.
		 * @return The recommended column.
		 */
		public abstract int getBestColumn(Player[][] state, Player player);
		public String toString() {
			return name();
		}
	}
	
	/**
	 * Adds a custom AI to the drop-down menus.
	 * @param ai Any custom AI instance.
	 */
	protected final void registerAi(AI ai) {
		ais.add(ai);
	}
	
	private final AI getAi(Player turn) {
		if (turn == Player.PLAYERX)
			return getAi(playerControlX.getSelectionModel().getSelectedItem());
		else if (turn == Player.PLAYERO)
			return getAi(playerControlO.getSelectionModel().getSelectedItem());
		return null;
	}
	private final AI getAi(String name) {
		for (AI ai: ais)
			if (ai.name().equals(name)) return ai;
		return null;
	}
	
	/** This is a dummy AI for human control. */
	private final class Human extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			return -1;
		}
	}
	
	/** This is a really naive AI that makes no attempt
	 * to win. It simply picks a random move. */
	private final class Random extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			java.util.ArrayList<Integer> range = new java.util.ArrayList<>();
			for (int c = 0; c < GRID_WIDTH; c++)
				range.add(c);
			java.util.Collections.shuffle(range);
			
			for (int c = 0; c < GRID_WIDTH; c++)
				if (top(state, range.get(c)) >= 0)
					return range.get(c);
			
			return -1;
		}
	}
	
	/** This AI will win if it can in the current move
	 * or keep the opponent from winning if it can in the
	 * opponent's next move. Otherwise it plays randomly. */
	private final class Naive extends AI {
		@Override
		public int getBestColumn(Player[][] state, Player player) {
			for (int c = 0; c < GRID_WIDTH; c++) {
				int r = top(state, c);
				if (r >= 0) {
					Player[][] hypothetical = state;
					hypothetical[r][c] = player;
					if (getWinner(hypothetical, r, c) == player)
						return c;
					Player opponent = (player == Player.PLAYERX ? Player.PLAYERO : Player.PLAYERX);
					hypothetical[r][c] = opponent;
					if (getWinner(hypothetical, r, c) == opponent)
						return c;
					hypothetical[r][c] = Player.NONE;
				}
			}
	
			return (new Random()).getBestColumn(state, player);
		}
	}
}
