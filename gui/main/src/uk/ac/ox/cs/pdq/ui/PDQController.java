package uk.ac.ox.cs.pdq.ui;

import static uk.ac.ox.cs.pdq.ui.PDQApplication.PLAN_DIRECTORY;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.PLAN_FILENAME_SUFFIX;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.PROOF_FILENAME_SUFFIX;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.PROPERTIES_SUFFIX;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.QUERY_DIRECTORY;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.QUERY_FILENAME_SUFFIX;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.SCHEMA_DIRECTORY;
import static uk.ac.ox.cs.pdq.ui.PDQApplication.SCHEMA_FILENAME_SUFFIX;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
//import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.LinearGuarded;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.ui.io.ObservableQueryReader;
import uk.ac.ox.cs.pdq.ui.io.ObservableSchemaReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryReader;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryWriter;
import uk.ac.ox.cs.pdq.ui.io.xml.ProofReader;
import uk.ac.ox.cs.pdq.ui.model.ObservablePlan;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;
import uk.ac.ox.cs.pdq.ui.proof.Proof;
import uk.ac.ox.cs.pdq.util.SanityCheck;

// TODO: Auto-generated Javadoc
/**
 * Controller class for the main PDQ window.
 * 
 * @author Julien Leblay
 * @author Mark Ridler
 *
 */
public class PDQController {

	/** PDQController's logger. */
	private static Logger log = Logger.getLogger(PDQController.class);

	/*
	 * JavaFX-specific initializations
	 */
	/** Used to protect selects from competing with each other */
	private boolean g_lock = true;
	
	/** A map of View Controllers. */
	public static HashMap<Stage, ViewController> g_viewControllerMap = new HashMap<>();

	/** Default icon for relations. */
	private final Image dbRelationIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/table.gif"));

	/** Icon for views. */
	private final Image dbViewIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/view.png"));

	/** Icon for online relations. */
	private final Image webRelationIcon = new Image(
			this.getClass().getResourceAsStream("/resources/icons/web-relation.png"));

	/** Default icon for dependency. */
	private final Image dependencyIcon = new Image(
			this.getClass().getResourceAsStream("/resources/icons/dependency.gif"));

	/** Icon for FK dependencies. */
	private final Image fkIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/fk-dependency.gif"));

	/** Icon for the edit buttons. */
	private final Image editIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/write.gif"));

	/** Icon for the planner buttons. */
	private final Image planIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/plan.gif"));

	/** Icon for the runtime buttons. */
	private final Image runIcon = new Image(this.getClass().getResourceAsStream("/resources/icons/run.gif"));

	/** The schemas tree view. */
	// Widgets
	@FXML
	private TreeView<String> schemasTreeView;

	/** The queries list view. */
	@FXML
	private ListView<ObservableQuery> queriesListView;

	/** The col search type. */
	@FXML
	private TableColumn<ObservablePlan, PlannerTypes> colSearchType;

	/** The col reasoning type. */
	@FXML
	private TableColumn<ObservablePlan, ReasoningTypes> colReasoningType;

	/** The col cost type. */
	@FXML
	private TableColumn<ObservablePlan, CostTypes> colCostType;

	/** The col cost. */
	@FXML
	private TableColumn<ObservablePlan, String> colCost;

	/** The settings timeout text field. */
	@FXML
	private TextField settingsTimeoutTextField;

	/** The settings max iterations text field. */
	@FXML
	private TextField settingsMaxIterationsTextField;

	/** The settings query match interval text field. */
	@FXML
	private TextField settingsQueryMatchIntervalTextField;

	/** The settings blocking interval text field. */
	@FXML
	private TextField settingsBlockingIntervalTextField;

	/** The settings output tuples text field. */
	@FXML
	private TextField settingsOutputTuplesTextField;

	/** The settings planner type list. */
	@FXML
	private ComboBox<PlannerTypes> settingsPlannerTypeList;

	/** The settings reasoning type list. */
	@FXML
	private ComboBox<ReasoningTypes> settingsReasoningTypeList;

	/** The settings cost type list. */
	@FXML
	private ComboBox<CostTypes> settingsCostTypeList;

	/** The query text area. */
	@FXML
	private TextArea queryTextArea;

	/** The query plan area. */
	@FXML
	private SplitPane queryPlanArea;

	/** The plan settings area. */
	@FXML
	private HBox planSettingsArea;

	/** The plan view area. */
	@FXML
	private ListView<Text> planViewArea;

	/** The proof view area. */
	@FXML
	private TextArea proofViewArea;

	/** The plans table view. */
	@FXML
	TableView<ObservablePlan> plansTableView;

	/** The settings executor type list. */
	@FXML
	ComboBox<String> settingsExecutorTypeList;

	/** The run planner button. */
	@FXML
	Button runPlannerButton;

	/** The run runtime button. */
	@FXML
	Button runRuntimeButton;

	/** The queries edit menu button. */
	@FXML
	private MenuButton queriesEditMenuButton;

	/** The schemas edit menu button. */
	@FXML
	private MenuButton schemasEditMenuButton;

	/** Queue containing the next widget to refresh on the interface. */
	private ConcurrentLinkedQueue<Object> dataQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Delete selected schemas.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void deleteSelectedSchemas(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			ObservableList<TreeItem<String>> selected = this.schemasTreeView.getSelectionModel().getSelectedItems();
			for (TreeItem<String> item : selected) {
				ObservableSchema schema = this.schemas.get(item.getValue());
				if (schema != null) {
					this.deleteSchema(schema);
					this.schemasTreeView.getRoot().getChildren().remove(item);
					continue;
				}
				schema = this.schemas.get(item.getParent().getValue());
				if (schema == null) {
					log.warn("Unable to identify item to delete '" + item + "'");
					continue;
				}
				Relation relation = schema.getSchema().getRelation(item.getValue());
				if (relation != null) {
					this.deleteRelation(schema, relation);
					this.reloadTreeItem(item.getParent(), schema);
					continue;
				}
				int index = this.currentSchemaViewitems.getParent().getChildren().indexOf(this.currentSchemaViewitems);
				index -= this.currentSchema.get().getSchema().getRelations().length;
				Dependency dependency = this.currentSchema.get().getSchema().getAllDependencies()[index];
				if (dependency != null) {
					// this.deleteDependency(schema, dependency);
					this.reloadTreeItem(item.getParent(), schema);
				}
			}
			this.schemasTreeView.getSelectionModel().clearSelection();
		}
	}

	/**
	 * Delete the schema and all associated query/plans from the system.
	 *
	 * @param schema
	 *            the schema
	 */
	private void deleteSchema(ObservableSchema schema) {
		if (schema != null) {
			List<ObservableQuery> qs = Lists.newArrayList(this.queries.get(schema.getName()));
			for (ObservableQuery q : qs) {
				this.deleteQuery(schema, q);
			}
			this.queries.remove(schema.getName());
			if (schema.equals(this.currentSchema.get())) {
				this.currentSchema.set(null);
			}
			this.schemas.remove(schema.getName());
			schema.destroy();
		}
	}

	/**
	 * Delete the given relation from the given schema.
	 *
	 * @param schema
	 *            the schema
	 * @param relation
	 *            the relation
	 */
	private void deleteRelation(ObservableSchema schema, Relation relation) {
		// TODO
		log.warn("Attempting to delete " + relation + " from " + schema);
	}

	/**
	 * Delete the given dependency from the given schema.
	 *
	 * @param schema
	 *            the schema
	 * @param dependency
	 *            the dependency
	 */
	/*
	 * private void deleteDependency(ObservableSchema schema, Dependency dependency)
	 * { // TODO log.warn("Attempting to delete " + dependency + " from " + schema);
	 * }
	 */

	/**
	 * Delete selected queries.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void deleteSelectedQueries(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			ObservableList<ObservableQuery> selected = this.queriesListView.getSelectionModel().getSelectedItems();
			for (int i = selected.size() - 1; i >= 0; i--) {
				this.deleteQuery(this.currentSchema.getValue(), selected.get(i));
			}
			this.queriesListView.getSelectionModel().clearSelection();
		}
	}

	/**
	 * Delete the query and all associated plans from the system.
	 *
	 * @param schema
	 *            the schema
	 * @param query
	 *            the query
	 */
	private void deleteQuery(ObservableSchema schema, ObservableQuery query) {
		if (query != null) {
			ObservableList<ObservablePlan> ps = this.plans.get(Pair.of(schema, query));
			for (ObservablePlan p : ps) {
				p.destroy();
			}
			ps.clear();
			this.plans.remove(Pair.of(schema, query));
			if (query.equals(this.currentQuery.get())) {
				this.currentQuery.set(null);
			}
			this.queries.get(schema.getName()).remove(query);
			query.destroy();
		}
	}

	/**
	 * Called from the query list view context menu. Opens an empty query.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void newQueryPressed(ActionEvent event) {
		Schema schema = this.currentSchema.get().getSchema();
		/*
		 * MR ObservableQuery query = new ObservableQuery("New Query", "", new
		 * QueryBuilder().setName("Q").addBodyAtom(
		 * schema.getRelations().iterator().next().createAtoms()).build());
		 * this.dataQueue.add(query);
		 */
	}

	/**
	 * Duplicate selected query pressed.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void duplicateSelectedQueryPressed(ActionEvent event) {
		ObservableList<ObservableQuery> selected = this.queriesListView.getSelectionModel().getSelectedItems();

		if (selected.size() == 1) {
			ObservableQuery selectedQuery = selected.get(0);
			ConjunctiveQuery query = (ConjunctiveQuery) selectedQuery.getFormula();
			/*
			 * MR ConjunctiveQuery cQuery = new ConjunctiveQuery(query.getHead(),
			 * query.getBody()); ObservableQuery obsQuery = new
			 * ObservableQuery(selectedQuery.getName() + " (copy)", "", cQuery);
			 * this.dataQueue.add(obsQuery);
			 */
		}
	}

	/**
	 * Delete selected plans.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void deleteSelectedPlans(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			ObservableList<ObservablePlan> selected = this.plansTableView.getSelectionModel().getSelectedItems();
			log.debug("Deleting " + selected);
			for (int i = selected.size() - 1; i >= 0; i--) {
				this.deletePlan(this.currentSchema.getValue(), this.currentQuery.getValue(), selected.get(i));
			}
			this.currentPlan.set(null);
			this.plansTableView.getSelectionModel().clearSelection();
		}
	}

	/**
	 * Delete the plan and associated configuration from the system.
	 *
	 * @param schema
	 *            the schema
	 * @param query
	 *            the query
	 * @param plan
	 *            the plan
	 */
	private void deletePlan(ObservableSchema schema, ObservableQuery query, ObservablePlan plan) {
		plan.destroy();
		this.plans.get(Pair.of(schema, query)).remove(plan);
	}

	/**
	 * Action that open's the planner window and start a new planning session.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openPlannerWindow(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try {
				Stage dialog = new Stage();
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initStyle(StageStyle.UTILITY);
				dialog.initOwner(this.getOriginatingWindow(event));
				ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
				FXMLLoader loader = new FXMLLoader(
						PDQApplication.class.getResource("/resources/layouts/planner-window.fxml"), bundle);
				Parent parent = (Parent) loader.load();
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setTitle(bundle.getString("planner.dialog.title"));

				// Set the currently selected schema/query/plan
				final PlannerController plannerController = loader.getController();
				ObservablePlan pl = this.currentPlan.get();
				pl.setPlannerType(this.settingsPlannerTypeList.getValue());
				pl.setChaserType(this.settingsReasoningTypeList.getValue());
				pl.setCostType(this.settingsCostTypeList.getValue());
				pl.setTimeout(toDouble(this.settingsTimeoutTextField.getText()));
				pl.setMaxIterations(toDouble(this.settingsMaxIterationsTextField.getText()));
				pl.setQueryMatchInterval(toInteger(this.settingsQueryMatchIntervalTextField.getText()));
				// pl.setBlockingInterval(toInteger(this.settingsBlockingIntervalTextField.getText()));
				plannerController.setPlan(pl);
				plannerController.setPlanQueue(this.dataQueue);
				plannerController.setSchema(this.currentSchema.get());
				plannerController.setQuery(this.currentQuery.get());
				parent.autosize();
				dialog.setOnCloseRequest((WindowEvent arg0) -> plannerController.interruptPlanningThreads());
				dialog.showAndWait();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new UserInterfaceException(e.getMessage());
			}
		}
	}

	/**
	 * To double.
	 *
	 * @param s
	 *            the s
	 * @return the double
	 */
	private static Double toDouble(String s) {
		try {
			return NumberFormat.getInstance().parse(s).doubleValue();
		} catch (ParseException e) {
			return Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * To integer.
	 *
	 * @param s
	 *            the s
	 * @return the integer
	 */
	private static Integer toInteger(String s) {
		try {
			return NumberFormat.getInstance().parse(s).intValue();
		} catch (ParseException e) {
			return null;
		}
	}
	
	
	private void focusState(Stage dialog, boolean value) {
	    if (value) {
	        
	    	// Focus gained, highlight both dependencies
	    	
			MultipleSelectionModel msm = schemasTreeView.getSelectionModel();
			Dependency[] dependencies = this.currentSchema.get().getSchema().getAllDependencies();
			int index = 0;
			for(TreeItem<String> ti : schemasTreeView.getRoot().getChildren())
			{
				for(TreeItem<String> ti2 : ti.getChildren())
				{
					if(ti2.getValue().equals("Dependencies"))
					{
						for(TreeItem<String> ti3 : ti2.getChildren())
						{
							int row = schemasTreeView.getRow(ti3);
							Dependency dependency = dependencies[index]; 
							index++;
							
							// Get both dependencies characteristic of a view
							
							ViewController viewController = g_viewControllerMap.get(dialog);
							View view = viewController.getView();
							
							Dependency dependency2 = view.getViewToRelationDependency();
							Dependency dependency3 = view.getRelationToViewDependency();
							
							if((dependency != null) && 
							   (((dependency2 != null) && dependency2.equals(dependency)) ||
							     (dependency3 != null) && dependency3.equals(dependency)))
							{
								// Disable select updates with g_lock flag.
								
								g_lock = false;
								msm.select(row);
								g_lock = true;
							}
						}
					}
				}
			}
			
			// Highlight view
			
			for(TreeItem<String> ti : schemasTreeView.getRoot().getChildren())
			{
				for(TreeItem<String> ti2 : ti.getChildren())
				{
					if(ti2.getValue().equals("Views"))
					{
						for(TreeItem<String> ti3 : ti2.getChildren())
						{
							ViewController viewController = g_viewControllerMap.get(dialog);
							if(ti3.getValue().contentEquals(viewController.getView().getName()))
							{
								// Disable select updates with g_lock flag.

								g_lock = false;
								msm.select(ti3);
								g_lock = true;
							}
						}
					}
				}
			}	
	    }
	}
	/**
	 * Action that open's either the relation or dependencies inspector window.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openSchemaDetails(MouseEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			if (event.getClickCount() > 1) {

				MultipleSelectionModel msm = schemasTreeView.getSelectionModel();
				ObservableList<TreeItem<String>> obl = msm.getSelectedItems();
				this.currentSchemaViewitems = (TreeItem<String>) msm.getSelectedItem();
				
				if((currentSchemaViewitems.getParent() == null) ||
				   (currentSchemaViewitems.getParent().getParent() == null) ||
				   (currentSchemaViewitems.getParent().getParent().valueProperty().get() == null))
				{		
					return;
				}
					
				ObservableSchema schema = this.schemas.get(this.currentSchemaViewitems.getValue());
				if (schema == null) {
					try {
						Relation relation = this.currentSchema.get().getSchema()
								.getRelation(this.currentSchemaViewitems.getValue());
						
						// Check position of the relation in the TreeItem<String> tree view
						
						if ((currentSchemaViewitems.getParent().valueProperty().get().equals("Relations")
								|| (currentSchemaViewitems.getParent().getParent().valueProperty().get()
										.equals("Services"))
								&& (relation != null))) {
							
							// Open the Stage dialog
							Stage dialog = new Stage();							
							dialog.initModality(Modality.NONE);
							dialog.initStyle(StageStyle.UTILITY);
							dialog.initOwner(this.getOriginatingWindow(event));
							ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
							FXMLLoader loader = new FXMLLoader(
									PDQApplication.class.getResource("/resources/layouts/relation-window.fxml"),
									bundle);
							Parent parent = (Parent) loader.load();
							Scene scene = new Scene(parent);
							dialog.setScene(scene);
							dialog.setTitle(bundle.getString("relation.dialog.title"));
							
							// Open the relation controller
							RelationController relationController = loader.getController();
							relationController.setRelation(relation);
							dialog.showAndWait();
							return;
						}
					} catch (Throwable e) {
						throw new UserInterfaceException(e);
					}

					try {
						View view = (View) this.currentSchema.get().getSchema()
								.getRelation(this.currentSchemaViewitems.getValue());
						if (currentSchemaViewitems.getParent().valueProperty().get().equals("Views")
								&& (view != null)) {
							
							// Scan through the list of dependencies to see which should be highlighted
							
							Dependency[] dependencies = this.currentSchema.get().getSchema().getAllDependencies();
							int index = 0;
							for(TreeItem<String> ti : schemasTreeView.getRoot().getChildren())
							{
								for(TreeItem<String> ti2 : ti.getChildren())
								{
									if(ti2.getValue().equals("Dependencies"))
									{
										for(TreeItem<String> ti3 : ti2.getChildren())
										{
											int row = schemasTreeView.getRow(ti3);
											if(index < dependencies.length)
											{
												Dependency dependency = dependencies[index]; 
												index++;
											
												// Get both dependencies characteristic of a view
											
												Dependency dependency2 = view.getViewToRelationDependency();
												Dependency dependency3 = view.getRelationToViewDependency();
											
												if((dependency != null) && 
												  (((dependency2 != null) && dependency2.equals(dependency)) ||
													(dependency3 != null) && dependency3.equals(dependency)))
												{
													// Disable select updates with g_lock flag.
												
													g_lock = false;
													msm.select(row);
													g_lock = true;
												}
											}
										}
									}
								}
							}
							
							// Open the Stage dialog
							Stage dialog = new Stage();

							dialog.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
							    focusState(dialog, newValue);
							});

							dialog.initModality(Modality.NONE);
							dialog.initStyle(StageStyle.UTILITY);
							dialog.initOwner(this.getOriginatingWindow(event));
							ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
							FXMLLoader loader = new FXMLLoader(
									PDQApplication.class.getResource("/resources/layouts/view-window.fxml"), bundle);
							Parent parent = (Parent) loader.load();
							Scene scene = new Scene(parent);
							dialog.setScene(scene);
							dialog.setTitle(bundle.getString("view.dialog.title"));
							
							// Open the view controller
							ViewController viewController = loader.getController();
							viewController.setView(view);
							g_viewControllerMap.put(dialog, viewController);
							dialog.showAndWait();
							return;
						}
					} catch (Throwable e) {
						throw new UserInterfaceException(e);
					}

					int index = this.currentSchemaViewitems.getParent().getChildren()
							.indexOf(this.currentSchemaViewitems);

					// Check dependency to load into the dependency controller
					Dependency dependency = this.currentSchema.get().getSchema().getAllDependencies()[index];
					if (currentSchemaViewitems.getParent().valueProperty().get().equals("Dependencies")
							&& (dependency != null)) {
						try {
							
							// Open the Stage dialog
							Stage dialog = new Stage();
							dialog.initModality(Modality.NONE);
							dialog.initStyle(StageStyle.UTILITY);
							dialog.initOwner(this.getOriginatingWindow(event));
							ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
							FXMLLoader loader = new FXMLLoader(
									PDQApplication.class.getResource("/resources/layouts/dependency-window.fxml"),
									bundle);
							Parent parent = (Parent) loader.load();
							Scene scene = new Scene(parent);
							dialog.setScene(scene);
							dialog.setTitle(bundle.getString("dependency.dialog.title"));

							// Set the currently selected schema/query/plan
							DependencyController dependencyController = loader.getController();
							dependencyController.setQueue(this.dataQueue);
							dependencyController.setDependency(dependency);
							ObservableSchema s = this.currentSchema.get();
							dependencyController.setSchema(s);
							dependencyController.setQueries(this.queries.get(s.getName()));

							dialog.showAndWait();
							return;
						} catch (IOException e) {
							throw new UserInterfaceException(e.getMessage());
						}
					}

					// Find the service that corresponds to currentSchemaViewItems
					Service service = null;
					for (Service sr : this.currentSchema.get().getServices()) {
						if (sr.getName().equals(currentSchemaViewitems.getValue())) {
							service = sr;
							break;
						}
					}
					
					// Check services in the list of services and when connected to a relation by access method
					
					if ((currentSchemaViewitems.getParent().valueProperty().get().equals("Services")
							|| currentSchemaViewitems.getParent().getParent().valueProperty().get().equals("Relations"))
							&& (service != null)) {
						try {
							Stage dialog = new Stage();
							dialog.initModality(Modality.NONE);
							dialog.initStyle(StageStyle.UTILITY);
							dialog.initOwner(this.getOriginatingWindow(event));
							ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
							FXMLLoader loader = new FXMLLoader(
									PDQApplication.class.getResource("/resources/layouts/service-window.fxml"), bundle);
							Parent parent = (Parent) loader.load();
							Scene scene = new Scene(parent);
							dialog.setScene(scene);
							dialog.setTitle(bundle.getString("service.dialog.title"));
							ServiceController serviceController = loader.getController();
							serviceController.setService(service);
							dialog.showAndWait();
							return;
						} catch (Exception e) {
							throw new UserInterfaceException(e);
						}
					}
				}
			}
		}
	}

	/**
	 * Action that open's the query inspector window.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openQueryDetails(MouseEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			if (event.getClickCount() > 1
					&& this.plans.get(Pair.of(this.currentSchema.get(), this.currentQuery.get())).isEmpty()) {
				try {
					Stage dialog = new Stage();
					dialog.initModality(Modality.WINDOW_MODAL);
					dialog.initStyle(StageStyle.UTILITY);
					dialog.initOwner(this.getOriginatingWindow(event));
					ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
					FXMLLoader loader = new FXMLLoader(
							PDQApplication.class.getResource("/resources/layouts/query-editor.fxml"), bundle);
					Parent parent = (Parent) loader.load();
					Scene scene = new Scene(parent);
					dialog.setScene(scene);
					dialog.setTitle(bundle.getString("query_editor.dialog.title"));

					QueryEditorController queryEditorController = loader.getController();

					queryEditorController.setQuery(this.currentQuery.get());
					queryEditorController.setSchema(this.currentSchema.get());
					queryEditorController.setQueriesListView(this.queriesListView);

					parent.autosize();
					dialog.showAndWait();
					this.saveQuery(this.currentQuery.get());
					ObservableList<ObservableQuery> list = this.queriesListView.getItems();
					this.queriesListView.setItems(null);
					this.queriesListView.setItems(list);
					return;
				} catch (IOException e) {
					throw new UserInterfaceException(e.getMessage());
				}
			}
		}
	}

	/**
	 * Action that open's the runtime window and start a new runtime session.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openRuntimeWindow(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try {
				Stage dialog = new Stage();
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initStyle(StageStyle.UTILITY);
				dialog.initOwner(this.getOriginatingWindow(event));
				ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
				FXMLLoader loader = new FXMLLoader(
						PDQApplication.class.getResource("/resources/layouts/runtime-window.fxml"), bundle);
				Parent parent = (Parent) loader.load();
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setTitle(bundle.getString("runtime.dialog.title"));

				// Set the currently selected schema/query/plan
				final RuntimeController runtimeController = loader.getController();
				runtimeController.setPlan(this.currentPlan.get());
				runtimeController.setSchema(this.currentSchema.get());
				runtimeController.setQuery(this.currentQuery.get());
				// runtimeController.setExecutorType(this.settingsExecutorTypeList.getValue());
				runtimeController.setTuplesLimit(toInteger(this.settingsOutputTuplesTextField.getText()));
				dialog.setOnCloseRequest((WindowEvent arg0) -> runtimeController.interruptRuntimeThreads());
				dialog.showAndWait();
			} catch (IOException e) {
				throw new UserInterfaceException("");
			}
		}
	}

	/**
	 * Action that open's the runtime window and start a new runtime session.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openImportSchemaWindow(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try {
				final Stage dialog = new Stage();
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initStyle(StageStyle.UTILITY);
				dialog.initOwner(this.getOriginatingWindow(event));
				ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
				FXMLLoader loader = new FXMLLoader(
						PDQApplication.class.getResource("/resources/layouts/import-dialog.fxml"), bundle);
				Parent parent = (Parent) loader.load();
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setTitle(bundle.getString("application.dialog.import.schema.title"));

				// Set the currently selected schema/query/plan
				ImportController importController = loader.getController();
				importController.setQueue(this.dataQueue);
				importController.setForbiddenNames(this.schemas.keySet());
				dialog.addEventHandler(KeyEvent.ANY, (KeyEvent e) -> {
					if (!e.isConsumed() && e.getCode() == KeyCode.ESCAPE) {
						e.consume();
						dialog.close();
					}
				});
				dialog.showAndWait();
			} catch (IOException e) {
				throw new UserInterfaceException(e);
			}
		}
	}

	/**
	 * Action that open's the runtime window and start a new runtime session.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openExportSchemaWindow(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try {
				final Stage dialog = new Stage();
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initStyle(StageStyle.UTILITY);
				dialog.initOwner(this.getOriginatingWindow(event));
				ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
				FXMLLoader loader = new FXMLLoader(
						PDQApplication.class.getResource("/resources/layouts/export-dialog.fxml"), bundle);
				Parent parent = (Parent) loader.load();
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setTitle(bundle.getString("application.dialog.export.schema.title"));

				// Set the currently selected schema/query/plan
				ExportController exportController = loader.getController();
				exportController.setQueue(this.dataQueue);
				exportController.setSchema(this.currentSchema.get());
				dialog.addEventHandler(KeyEvent.ANY, (KeyEvent e) -> {
					if (!e.isConsumed() && e.getCode() == KeyCode.ESCAPE) {
						e.consume();
						dialog.close();
					}
				});
				dialog.showAndWait();
			} catch (IOException e) {
				throw new UserInterfaceException(e);
			}
		}
	}


	/**
	 * Action that saves the current query.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void saveSelectedQuery(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try
			{
				String str = this.queryTextArea.textProperty().get();
				SQLLikeQueryReader qr = new SQLLikeQueryReader(this.currentSchema.get().getSchema());
				ConjunctiveQuery cjq = qr.fromString(str);
				this.currentQuery.get().setQuery(cjq);
				saveQuery(this.currentQuery.get());
		    	Alert alert = new Alert(AlertType.INFORMATION);
		    	alert.setTitle("Result Dialog");
		    	alert.setHeaderText(null);
		    	alert.setContentText("Successfully saved");
		    	alert.showAndWait();
			}
			catch(Exception e)
			{
				return;				
			}
		}
	}
	
	/**
	 * Action that saves as the current query.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void saveAsSelectedQuery(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try
			{
				String str = this.queryTextArea.textProperty().get();
				SQLLikeQueryReader qr = new SQLLikeQueryReader(this.currentSchema.get().getSchema());
				ConjunctiveQuery cjq = qr.fromString(str);
				this.currentQuery.get().setQuery(cjq);
				saveAsQuery(this.currentQuery.get());
			}
			catch(Exception e)
			{
				return;				
			}
		}
	}
	
	/**
	 * Action that open's the runtime window and start a new runtime session.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void openImportQueryWindow(ActionEvent event) {
		if (!event.isConsumed()) {
			event.consume();
			try {
				final Stage dialog = new Stage();
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initStyle(StageStyle.UTILITY);
				dialog.initOwner(this.getOriginatingWindow(event));
				ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
				FXMLLoader loader = new FXMLLoader(
						PDQApplication.class.getResource("/resources/layouts/import-dialog.fxml"), bundle);
				Parent parent = (Parent) loader.load();
				Scene scene = new Scene(parent);
				dialog.setScene(scene);
				dialog.setTitle(bundle.getString("application.dialog.import.query.title"));

				// Set the currently selected schema/query/plan
				ImportController importController = loader.getController();
				importController.setSchema(this.currentSchema.get());
				importController.setQueue(this.dataQueue);
				dialog.addEventHandler(KeyEvent.ANY, (KeyEvent e) -> {
					if (!e.isConsumed() && e.getCode() == KeyCode.ESCAPE) {
						e.consume();
						dialog.close();
					}
				});
				dialog.showAndWait();
			} catch (IOException e) {
				throw new UserInterfaceException(e);
			}
		}
	}

	/**
	 * Action that create a new planning configuration in the plan table view.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	void newSettings(ActionEvent event) {
		PlannerParameters params = new PlannerParameters();
		params.setPlannerType(PlannerTypes.LINEAR_OPTIMIZED);
		params.setTimeout("\u22E1");
		params.setMaxIterations("\u22E1");
		CostParameters costParams = new CostParameters();
		costParams.setCostType(CostTypes.SIMPLE_CONSTANT);
		ReasoningParameters reasoningParams = new ReasoningParameters();
		reasoningParams.setReasoningType(ReasoningParameters.ReasoningTypes.RESTRICTED_CHASE);
		ObservablePlan p = new ObservablePlan(params, costParams, reasoningParams);
		this.currentPlan.set(p);
		this.plans.get(Pair.of(this.currentSchema.get(), this.currentQuery.get())).add(p);
		this.plansTableView.getSelectionModel().select(p);
	}

	/**
	 * Gets the originating window.
	 *
	 * @param e
	 *            the e
	 * @return the window the given event was generated from.
	 */
	private Window getOriginatingWindow(Event e) {
		Object o = e.getSource();
		if (o instanceof Node) {
			return ((Node) o).getScene().getWindow();
		}
		if (o instanceof MenuItem) {
			return ((MenuItem) o).getParentPopup().getOwnerWindow();
		}
		throw new IllegalStateException("Unable to determine originating window for event " + e);
	}

	/**
	 * User interface initialization.
	 */
	@FXML
	void initialize() {
		assert this.colReasoningType != null : "fx:id=\"colReasoningType\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.colCost != null : "fx:id=\"colCost\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.colCostType != null : "fx:id=\"colCostType\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.colSearchType != null : "fx:id=\"colSearchType\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.planSettingsArea != null : "fx:id=\"planSettingsArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.planViewArea != null : "fx:id=\"planViewArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.proofViewArea != null : "fx:id=\"proofViewArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.plansTableView != null : "fx:id=\"plansTableView\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.queriesEditMenuButton != null : "fx:id=\"queriesEditMenuButton\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.schemasEditMenuButton != null : "fx:id=\"schemasEditMenuButton\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.queriesListView != null : "fx:id=\"queriesListView\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.queryPlanArea != null : "fx:id=\"queryPlanArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.queryTextArea != null : "fx:id=\"queryTextArea\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.runPlannerButton != null : "fx:id=\"runPlannerButton\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.runRuntimeButton != null : "fx:id=\"runRuntimeButton\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.schemasTreeView != null : "fx:id=\"schemasTreeView\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsBlockingIntervalTextField != null : "fx:id=\"settingsBlockingIntervalTextField\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsReasoningTypeList != null : "fx:id=\"settingsReasoningTypeList\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsCostTypeList != null : "fx:id=\"settingsCostTypeList\" was not injected: check your FXML file 'root-window.fxml'.";
		// MR assert this.settingsExecutorTypeList != null :
		// "fx:id=\"settingsExecutorTypeList\" was not injected: check your FXML file
		// 'root-window.fxml'.";
		assert this.settingsOutputTuplesTextField != null : "fx:id=\"settingsOutputTuplesTextField\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsMaxIterationsTextField != null : "fx:id=\"settingsMaxIterationsTextField\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsPlannerTypeList != null : "fx:id=\"settingsPlannerTypeList\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsQueryMatchIntervalTextField != null : "fx:id=\"settingsQueryMatchIntervalTextField\" was not injected: check your FXML file 'root-window.fxml'.";
		assert this.settingsTimeoutTextField != null : "fx:id=\"settingsTimeoutTextField\" was not injected: check your FXML file 'root-window.fxml'.";

		this.runPlannerButton.setGraphic(new ImageView(this.planIcon));
		this.runRuntimeButton.setGraphic(new ImageView(this.runIcon));
		this.queriesEditMenuButton.setGraphic(new ImageView(this.editIcon));
		this.schemasEditMenuButton.setGraphic(new ImageView(this.editIcon));

		this.configureSchemas();
		this.configureQueries();
		this.configurePlans();
		this.configureSettings();
	}

	/**
	 * Schema-specific widget initialisations.
	 */
	private void configureSchemas() {
		this.loadSchemas();
		this.loadTreeItems();
		this.schemasTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.schemasTreeView.getSelectionModel().selectedItemProperty().addListener(this.schemaSelected);
	}

	/**
	 * Query-specific widget initialisations.
	 */
	private void configureQueries() {
		this.loadQueries();
		this.queriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.queriesListView.getSelectionModel().selectedItemProperty().addListener(this.querySelected);
		this.queryPlanArea.visibleProperty().bind(Bindings.isNotNull(this.currentQuery));
		this.queriesEditMenuButton.disableProperty().bind(Bindings.isNull(this.currentSchema));
/* MR		this.queryTextArea.textProperty().bind(Bindings.createStringBinding(() -> {
			ObservableQuery q = PDQController.this.currentQuery.get();
			boolean b = this.queryTextArea.textProperty().isBound();
			return (q != null) ? SQLLikeQueryWriter.convert(q.getFormula()) : "";
		}, this.currentQuery));*/
	}

	/**
	 * Plan-specific widget initialisations.
	 */
	private void configurePlans() {
		this.loadPlans();
		this.currentPlan.addListener(this.currentPlanChanged);
		this.plansTableView.getSelectionModel().selectedItemProperty().addListener(this.planSelected);
		this.plansTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.plansTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.colSearchType.setCellValueFactory(new PropertyValueFactory<ObservablePlan, PlannerTypes>("plannerType"));
		this.colReasoningType
				.setCellValueFactory(new PropertyValueFactory<ObservablePlan, ReasoningTypes>("reasoningType"));
		this.colCostType.setCellValueFactory(new PropertyValueFactory<ObservablePlan, CostTypes>("costType"));
		this.colCost.setCellValueFactory(new PropertyValueFactory<ObservablePlan, String>("cost"));
		this.planSettingsArea.visibleProperty().bind(Bindings.isNotNull(this.currentPlan));
	}

	/**
	 * Planner settings-specific widget initializations.
	 */
	private void configureSettings() {
		this.settingsPlannerTypeList.getItems().clear();
		this.settingsReasoningTypeList.getItems().clear();
		this.settingsCostTypeList.getItems().clear();
		// MR this.settingsExecutorTypeList.getItems().clear();
		this.settingsPlannerTypeList.getItems().addAll(PlannerTypes.LINEAR_GENERIC, PlannerTypes.LINEAR_OPTIMIZED,
				PlannerTypes.LINEAR_KCHASE);
		this.settingsReasoningTypeList.getItems().addAll(ReasoningTypes.RESTRICTED_CHASE,
				ReasoningTypes.KTERMINATION_CHASE);
		this.settingsCostTypeList.getItems().addAll(CostTypes.values());
		// MR this.settingsExecutorTypeList.getItems().addAll(ExecutorTypes.values());
		// MR
		// this.settingsExecutorTypeList.getSelectionModel().select(ExecutorTypes.PIPELINED);
	}

	/**
	 * Behaviour triggered when a schema is selected.
	 */
	private final ChangeListener<TreeItem<String>> schemaSelected = (
			ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,
			TreeItem<String> newValue) -> {
		if (newValue != null) {
			PDQController.this.schemaSelected(newValue);
		}
	};

	/**
	 * Selects a schema from the UI.
	 *
	 * @param newValue
	 *            the new value
	 */
	void schemaSelected(TreeItem<String> newValue) {
		String oldName = null;
		if (this.currentSchema.get() != null) {
			oldName = this.currentSchema.get().getName();
		}

		String schemaName = newValue.getValue();

		ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
		Boolean isNavigator = newValue.getValue().equals(bundle.getString("application.schema.schemas.relations"))
				|| newValue.getValue().equals(bundle.getString("application.schema.schemas.views"))
				|| newValue.getValue().equals(bundle.getString("application.schema.schemas.services"))
				|| newValue.getValue().equals(bundle.getString("application.schema.schemas.dependencies"));

		if (!this.schemas.containsKey(schemaName)) {
			schemaName = newValue.getParent().getValue();
		}
		if (!this.schemas.containsKey(schemaName)) {
			schemaName = newValue.getParent().getParent().getValue();
		}
		if (!this.schemas.containsKey(schemaName)) {
			schemaName = newValue.getParent().getParent().getParent().getValue();
		}
		this.currentSchema.set(this.schemas.get(schemaName));
		this.queriesListView.setItems(this.queries.get(schemaName));
		if(g_lock)
		{
			this.currentSchemaViewitems = newValue;	
		}
		if (!schemaName.equals(oldName)) {
			this.queriesListView.getSelectionModel().clearSelection();
			this.currentQuery.set(null);
			this.currentPlan.set(null);
		}
	}

	/**
	 * Behaviour triggered when a query is selected.
	 */
	private final ChangeListener<ObservableQuery> querySelected = (
			ObservableValue<? extends ObservableQuery> observable, ObservableQuery oldValue,
			ObservableQuery newValue) -> {
		PDQController.this.currentQuery.set(newValue);
		if (newValue != null) {
			SimpleObjectProperty<ObservablePlan> plan = this.currentPlan;
			ObservableList<ObservablePlan> list = PDQController.this.plans
					.get(Pair.of(PDQController.this.currentSchema.get(), newValue));
			PDQController.this.plansTableView.setItems(list);
			this.queryTextArea.textProperty().set(SQLLikeQueryWriter.convert(this.currentQuery.get().getFormula(), this.currentSchema.get().getSchema()));
		}
		else
		{
			this.queryTextArea.textProperty().set("");
		}
	};

	/**
	 * Behaviour triggered when a plan is selected.
	 */
	private final ChangeListener<ObservablePlan> planSelected = (ObservableValue<? extends ObservablePlan> observable,
			ObservablePlan oldValue, ObservablePlan newValue) -> {
		PDQController.this.currentPlan.set(newValue);
	};

	/**
	 * Behaviour triggered when current plan changes.
	 */
	private final ChangeListener<ObservablePlan> currentPlanChanged = (
			ObservableValue<? extends ObservablePlan> observable, ObservablePlan oldValue, ObservablePlan newValue) -> {
		if (newValue != null) {
			PDQController.this.savePlan(newValue);

			Plan plan = newValue.getPlan();
			// MR PDQController.this.settingsExecutorTypeList.setDisable(plan == null);
			PDQController.this.runRuntimeButton.setDisable(plan == null);
			PDQController.this.setSettingsEditable(plan == null);
			PDQController.this.displayPlan(plan);
			PDQController.this.displayProof(newValue.getProof());
			PDQController.this.displaySettings(newValue);
		} else {
			PDQController.this.plansTableView.getSelectionModel().clearSelection();
		}
	};

	/**
	 * Display plan.
	 *
	 * @param p
	 *            the p
	 */
	void displayPlan(Plan p) {
		PDQController.this.planViewArea.getItems().clear();
		if(p != null)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			new PrintStream(bos).println(p.toString());
			for (String line: bos.toString().split("\n"))
			{
				Text t = new Text(line);
				PDQController.this.planViewArea.getItems().add(t);
			}
		}
	}

	/**
	 * Display proof.
	 *
	 * @param p
	 *            the p
	 */
	void displayProof(Proof p) {
		PDQController.this.proofViewArea.clear();
		if (p != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			new PrintStream(bos).println(p.toString());
			PDQController.this.proofViewArea.setText(bos.toString());
		}
	}

	/**
	 * Display settings.
	 *
	 * @param p
	 *            the p
	 */
	void displaySettings(ObservablePlan p) {
		PDQController.this.settingsTimeoutTextField.setText(PDQController.nullToEmpty(p.getTimeout()));
		PDQController.this.settingsMaxIterationsTextField.setText(PDQController.nullToEmpty(p.getMaxIterations()));
		PDQController.this.settingsQueryMatchIntervalTextField
				.setText(PDQController.nullToEmpty(p.getQueryMatchInterval()));
		// PDQController.this.settingsBlockingIntervalTextField.setText(PDQController.nullToEmpty(p.getBlockingInterval()));
		PDQController.this.settingsPlannerTypeList.getSelectionModel().select(p.getPlannerType());
		PDQController.this.settingsReasoningTypeList.getSelectionModel().select(p.getReasoningType());
		PDQController.this.settingsCostTypeList.getSelectionModel().select(p.getCostType());
	}

	/**
	 * Sets the settings editable.
	 *
	 * @param editable
	 *            the new settings editable
	 */
	void setSettingsEditable(boolean editable) {
		PDQController.this.settingsTimeoutTextField.setEditable(editable);
		PDQController.this.settingsMaxIterationsTextField.setEditable(editable);
		PDQController.this.settingsQueryMatchIntervalTextField.setEditable(editable);
		PDQController.this.settingsBlockingIntervalTextField.setEditable(editable);
		PDQController.this.settingsPlannerTypeList.setDisable(!editable);
		PDQController.this.settingsReasoningTypeList.setDisable(!editable);
		PDQController.this.settingsCostTypeList.setDisable(!editable);
	}

	/** The application's user-specific work directory. */
	private final File workDirectory;

	/** The user's schema collection. */
	final Map<String, ObservableSchema> schemas = new LinkedHashMap<>();

	/** The user's query collection. */
	final Map<String, ObservableList<ObservableQuery>> queries = new HashMap<>();

	/** The user's plan collection. */
	final Map<Pair<ObservableSchema, ObservableQuery>, ObservableList<ObservablePlan>> plans = new HashMap<>();

	/** The schema currently selected. */
	SimpleObjectProperty<ObservableSchema> currentSchema = new SimpleObjectProperty<>();

	/** The schema currently selected tree cells. */
	private TreeItem<String> currentSchemaViewitems;

	/** The query currently selected. */
	SimpleObjectProperty<ObservableQuery> currentQuery = new SimpleObjectProperty<>();

	/** The plan currently selected. */
	SimpleObjectProperty<ObservablePlan> currentPlan = new SimpleObjectProperty<>();

	/** A map of old vs new variable names. */
	HashMap<String, String> map = new HashMap<>();
	
	/**
	 * Default construction, sets up the work directory, and loads all of its
	 * content.
	 *
	 * @throws UserInterfaceException
	 *             the user interface exception
	 */
	public PDQController() throws UserInterfaceException {
		this.workDirectory = PDQApplication.setupWorkDirectory();
		this.prepareTimeline();
	}

	/**
	 * Null to empty.
	 *
	 * @param o
	 *            the o
	 * @return the string
	 */
	private static String nullToEmpty(Object o) {
		String result = String.valueOf(o).trim();
		if (o == null || result.isEmpty()) {
			return "N/A";
		}
		return result;
	}

	/**
	 * Loads schemas present in the work directory.
	 */
	private void loadSchemas() {
		File schemaDir = new File(this.workDirectory.getAbsolutePath() + '/' + SCHEMA_DIRECTORY);
		for (File schemaFile : listFiles(schemaDir, "", SCHEMA_FILENAME_SUFFIX)) {
			ObservableSchemaReader schemaReader = new ObservableSchemaReader();
			ObservableSchema s = schemaReader.read(schemaFile);
			try
			{
				SanityCheck.sanityCheck(s.getSchema());
			}
			catch(Exception e)
			{
				throw new UserInterfaceException(schemaFile.getName() + "; " + e.getMessage());
			}
			s.setFile(schemaFile);
			this.schemas.put(s.getName(), s);
		}
	}
	
	/**
	 * Saves a schema.
	 *
	 * @param schema
	 *            the schema
	 */
	private void saveSchema(ObservableSchema schema) {
		File file = schema.getFile();
		if (file == null) {
			String filename = null;
			int i = 0;
			do {
				filename = this.workDirectory.getAbsolutePath() + '/' + SCHEMA_DIRECTORY + '/' + (i++)
						+ SCHEMA_FILENAME_SUFFIX;
			} while ((file = new File(filename)).exists());
			schema.setFile(file);
		}
		schema.store();
	}

	/**
	 * Saves a query.
	 *
	 * @param query
	 *            the query
	 */
	private void saveQuery(ObservableQuery query) {
		File file = query.getFile();
		if (file == null) {
			String filename = null;
			int i = 0;
			do {
				filename = this.workDirectory.getAbsolutePath() + '/' + QUERY_DIRECTORY + '/'
						+ makePrefix(this.currentSchema.get()) + (i++) + QUERY_FILENAME_SUFFIX;
			} while ((file = new File(filename)).exists());
			query.setFile(file);
		}
		query.store();
	}

	/**
	 * Saves as a query.
	 *
	 * @param query
	 *            the query
	 */
	private void saveAsQuery(ObservableQuery query) {
		try {
			final Stage dialog = new Stage();
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initStyle(StageStyle.UTILITY);
			//dialog.initOwner(this.getOriginatingWindow(event));
			ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
			FXMLLoader loader = new FXMLLoader(
					PDQApplication.class.getResource("/resources/layouts/saveas-dialog.fxml"), bundle);
			Parent parent = (Parent) loader.load();
			Scene scene = new Scene(parent);
			dialog.setScene(scene);
			dialog.setTitle(bundle.getString("application.dialog.saveas.query.title"));

			// Set the currently selected schema/query/plan
			SaveAsController saveasController = loader.getController();
			saveasController.setSchema(this.currentSchema.get());
			saveasController.setQueue(this.dataQueue);
			saveasController.saveAs(query);
			dialog.addEventHandler(KeyEvent.ANY, (KeyEvent e) -> {
				if (!e.isConsumed() && e.getCode() == KeyCode.ESCAPE) {
					e.consume();
					dialog.close();
				}
			});
			dialog.showAndWait();
		} catch (IOException e) {
			throw new UserInterfaceException(e);
		}
	}

/**
	 * Saves a plan.
	 *
	 * @param plan
	 *            the plan
	 */
	void savePlan(ObservablePlan plan) {
		File file = plan.getPlanFile();

		if (file == null) {
			String filename = null;
			int i = 0;
			do {
				String prefix = makePrefix(this.currentQuery.get()) + (i++);
				filename = this.workDirectory.getAbsolutePath() + '/' + PLAN_DIRECTORY + '/' + prefix
						+ PLAN_FILENAME_SUFFIX;
			} while ((file = new File(filename)).exists());
			plan.setPlanFile(file);
			plan.setSettingsFile(new File(replaceSuffix(file, PLAN_FILENAME_SUFFIX, PROPERTIES_SUFFIX)));
		}
		File proofFile = plan.getProofFile();
		if (proofFile == null) {
			plan.setProofFile(new File(replaceSuffix(file, PLAN_FILENAME_SUFFIX, PROOF_FILENAME_SUFFIX)));
		}
		plan.store();
	}

	/**
	 * Loads queries present in the work directory.
	 */
	private void loadQueries() {
		
		// Open the query directory
		
		File queryDir = new File(this.workDirectory.getAbsolutePath() + '/' + QUERY_DIRECTORY);
		for (ObservableSchema s : this.schemas.values()) {
			ObservableList<ObservableQuery> qs = this.queries.get(s.getName());
			if (qs == null) {
				qs = FXCollections.observableArrayList();
				this.queries.put(s.getName(), qs);
			}
			
			// List files in the query directory
			
			for (File queryFile : listFiles(queryDir, makePrefix(s), QUERY_FILENAME_SUFFIX)) {
				try (FileInputStream in = new FileInputStream(queryFile.getAbsolutePath())) {
					ObservableQueryReader queryReader = new ObservableQueryReader(s.getSchema());
					ObservableQuery q = queryReader.read(queryFile);
					q.setFile(queryFile);
					qs.add(q);
				} catch (IOException e) {
					throw new UserInterfaceException(e.getMessage(), e);
				}
			}
			
			// Sanity check queries against schema
			for(ObservableQuery q : qs)
			{
				try
				{
					SanityCheck.sanityCheck(q.getQuery(), s.getSchema());
				} catch (Exception e) {
					throw new UserInterfaceException(q.getName() + "; " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Make prefix.
	 *
	 * @param schema
	 *            the schema
	 * @return the string
	 */
	private static String makePrefix(ObservableSchema schema) {
		return schema.getFile().getName().replace(SCHEMA_FILENAME_SUFFIX, "") + "_";
	}

	/**
	 * Loads queries present in the work directory.
	 */
	private void loadPlans() {
		
		// Find plan based on schema and query
		
		File planDir = new File(this.workDirectory.getAbsolutePath() + '/' + PLAN_DIRECTORY);
		for (ObservableSchema s : this.schemas.values()) {
			for (ObservableQuery q : this.queries.get(s.getName())) {
				ObservableList<ObservablePlan> ps = this.plans.get(Pair.of(s, q));
				if (ps == null) {
					ps = FXCollections.observableArrayList();
					this.plans.put(Pair.of(s, q), ps);
				}
				
				// For all plan files in the planDir go  to the CostIOManager
				
				for (File planFile : listFiles(planDir, makePrefix(q), PLAN_FILENAME_SUFFIX)) {
					try (FileInputStream in = new FileInputStream(planFile.getAbsolutePath())) {
						File settings = new File(replaceSuffix(planFile, PLAN_FILENAME_SUFFIX, PROPERTIES_SUFFIX));
						if (!settings.exists()) {
							log.warn("Plan '" + planFile + "' has no associated parameters. Skipping.");
							continue;
						}
						Schema schema = s.getSchema();
						RelationalTerm p1 = CostIOManager.readRelationalTermFromRelationaltermWithCost(planFile,
								schema);
						PlannerParameters q1 = new PlannerParameters(settings);
						CostParameters r1 = new CostParameters(settings);
						ReasoningParameters s1 = new ReasoningParameters(settings);
						ObservablePlan p = new ObservablePlan(p1, q1, r1, s1);
						p.setPlanFile(planFile);
						p.setSettingsFile(settings);
						
						// Read the proof file
						
						File proof = new File(replaceSuffix(planFile, PLAN_FILENAME_SUFFIX, PROOF_FILENAME_SUFFIX));
						if (proof.exists()) {
							try (FileInputStream prIn = new FileInputStream(proof.getAbsolutePath())) {
								p.setProofFile(proof);
								ProofReader proofReader = new ProofReader(schema);
								p.setProof(proofReader.read(prIn));
							}
						}
						ps.add(p);
					} catch (Exception e) {
						log.warn("IO exception caught file loading file '" + planFile + "'. " + e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Make prefix.
	 *
	 * @param query
	 *            the query
	 * @return the string
	 */
	private static String makePrefix(ObservableQuery query) {
		return query.getFile().getName().replace(QUERY_FILENAME_SUFFIX, "") + "_";
	}

	/**
	 * Replace suffix.
	 *
	 * @param f
	 *            the f
	 * @param oldSuffix
	 *            the old suffix
	 * @param newSuffix
	 *            the new suffix
	 * @return the string
	 */
	private static String replaceSuffix(File f, String oldSuffix, String newSuffix) {
		return f.getParentFile().getAbsolutePath() + '/' + f.getName().replace(oldSuffix, newSuffix);
	}

	/**
	 * Loads the schema into the left-hand side tree view.
	 */
	private void loadTreeItems() {
		TreeItem<String> root = new TreeItem<>(null);
		this.schemasTreeView.setRoot(root);
		this.schemasTreeView.setShowRoot(false);
		ArrayList<ObservableSchema> list = new ArrayList<>();
		for (ObservableSchema s : this.schemas.values()) {
			list.add(s);
		}
		for (ObservableSchema s : list) {
			this.loadTreeItem(s);
		}
	}

	/**
	 * Loads the schema into the left-hand side tree view.
	 *
	 * @param s
	 *            the s
	 * @return the tree item
	 */
	private TreeItem<String> loadTreeItem(ObservableSchema s) {
		TreeItem<String> result = new TreeItem<>(s.getName());
		this.schemasTreeView.getRoot().getChildren().add(result);
		this.reloadTreeItem(result, s);
		return result;
	}

	// Replace symbol _x with attribute name for every variable in the atoms
	
	private void processDependencyBodyOrHeadAtoms(Atom[] atoms, Atom[] atoms2, Relation[] relations)
	{
		// Iterate over all atoms
			
		for(int a = 0; a < atoms.length; a++)
		{
			Atom atom = atoms[a];
			Predicate predicate = atom.getPredicate();
			Term[] terms = atom.getTerms();
			Term[] terms2 = new Term[terms.length];
			
			// Check that relation name equals predicate name
			
			for (Relation relation : relations)
			{
				if(relation.getName().equals(predicate.getName()))
				{
					Attribute[] attributes = relation.getAttributes();
					
					// Iterate over all terms
					
					for(int t = 0; t < terms.length; t++)
					{
						Term term = terms[t];
						if(t < attributes.length)
						{
							Attribute attribute = attributes[t];
							if(term instanceof Variable)
							{
								Variable variable = (Variable) term;
								String value = map.get(variable.getSymbol());
								if(value == null)
								{
									// This is a new variable so name it after the attribute
									
									map.put(variable.getSymbol(), attribute.getName());
									terms2[t] = Variable.create(attribute.getName());
								}
								else
								{
									// Reuse the existing variable name
									
									terms2[t] = Variable.create(value);									
								}
							}
							else if(term instanceof Constant)
							{
								Constant tc =  (Constant) term;
								String value = map.get(tc.toString());
								if(value == null)
								{
									// This is a new variable so name it after the attribute
									
									map.put(tc.toString(), attribute.getName());
									terms2[t] = Variable.create(attribute.getName());
								}
								else
								{
									// Reuse the existing variable name
									
									terms2[t] = Variable.create(value);									
								}
							}
						}
					}
				}
			}
			Atom atom2 = Atom.create(predicate, terms2);
			atoms2[a] = atom2;
		}
	}
	
	/**
	 * Reloads the schema into the left-hand side tree view.
	 *
	 * @param item
	 *            the item
	 * @param s
	 *            the s
	 */
	private void reloadTreeItem(TreeItem<String> item, ObservableSchema s) {
		item.getChildren().clear();

		ResourceBundle bundle = ResourceBundle.getBundle("resources.i18n.ui");
		TreeItem<String> relations = new TreeItem<>(bundle.getString("application.schema.schemas.relations"),
				new ImageView(this.dbRelationIcon));
		TreeItem<String> services = new TreeItem<>(bundle.getString("application.schema.schemas.services"),
				new ImageView(this.webRelationIcon));
		TreeItem<String> views = new TreeItem<>(bundle.getString("application.schema.schemas.views"),
				new ImageView(this.dbViewIcon));
		TreeItem<String> dependencies = new TreeItem<>(bundle.getString("application.schema.schemas.dependencies"),
				new ImageView(this.dependencyIcon));

		// For all relations choose an icon depending on view or not
		
		Relation[] relationz = s.getSchema().getRelations();
		Relation[] relationz2 = new Relation[relationz.length];
		for (int z = 0; z < relationz.length; z++) {
			Relation r = relationz[z];
			ImageView imageView = null;
			TreeItem ti;
			if (r instanceof View) {
				imageView = new ImageView(this.dbViewIcon);
				ti = new TreeItem<>(r.getName(), imageView);
				views.getChildren().add(ti);
				View v = (View) r;
								
				// Process the ViewToRelation dependency

				Dependency viewToRelation = v.getViewToRelationDependency();
				Atom[] bodyatoms = viewToRelation.getBodyAtoms();
				Atom[] bodyatoms2 = new Atom[bodyatoms.length];
				processDependencyBodyOrHeadAtoms(bodyatoms, bodyatoms2, relationz);
				Atom[] headatoms = viewToRelation.getHeadAtoms();
				Atom[] headatoms2 = new Atom[headatoms.length];
				processDependencyBodyOrHeadAtoms(headatoms, headatoms2, relationz);
				if(viewToRelation instanceof LinearGuarded)
				{
					LinearGuarded viewToRelation2;
					viewToRelation2 = LinearGuarded.create(bodyatoms2, headatoms2, viewToRelation.getName());
					v.setViewToRelationDependency(viewToRelation2);
				}
				
			} else {
				imageView = new ImageView(this.dbRelationIcon);
				ti = new TreeItem<>(r.getName(), imageView);
				relations.getChildren().add(ti);
			}
			
			// Find the service related to the relation by access method name
			
			boolean found = false;
			for (AccessMethodDescriptor a : r.getAccessMethods()) {
				for (Service sr : s.getServices()) {
					for (RESTExecutableAccessMethodSpecification ream : sr.getAccessMethod()) {
						if (ream.getName().equals(a.getName())) {
							imageView = new ImageView(this.webRelationIcon);
							ti.getChildren().add(new TreeItem<>(sr.getName(), imageView));
							found = true;
						}
						if (found)
							break;
					}
					if (found)
						break;
				}
				if (found)
					break;
			}
		}
		
		// Find the relation related to the service by access method name
		
		for (Service sr : s.getServices()) {
			ImageView imageView = new ImageView(this.webRelationIcon);
			TreeItem ti = new TreeItem<>(sr.getName(), imageView);
			services.getChildren().add(ti);
			boolean found = false;
			for (Relation r : s.getSchema().getRelations()) {
				for (AccessMethodDescriptor a : r.getAccessMethods()) {
					for (RESTExecutableAccessMethodSpecification ream : sr.getAccessMethod()) {
						if (ream.getName().equals(a.getName())) {
							imageView = new ImageView(this.dbRelationIcon);
							ti.getChildren().add(new TreeItem<>(r.getName(), imageView));
							found = true;
						}
						if (found)
							break;
					}
					if (found)
						break;
				}
				if (found)
					break;
			}
		}
		
		
		Dependency[] dependencys = s.getSchema().getAllDependencies();
		Dependency[] dependencys2 = new Dependency[dependencys.length];
		
		// For all dependencies 
		
		for (int d = 0; d < dependencys.length; d++) {
			Dependency ic = dependencys[d];
			
			// Choose the icon based on TGD and isGuarded()
			
			ImageView imageView = null;
			if (ic instanceof TGD && ((TGD) ic).isGuarded()) {
				imageView = new ImageView(this.fkIcon);
			} else {
				imageView = new ImageView(this.dependencyIcon);
			}
			
			// Replace symbol _x with attribute name for every variable in the body atoms
			
			Atom[] bodyatoms = ic.getBodyAtoms();
			Atom[] bodyatoms2 = new Atom[bodyatoms.length];
			processDependencyBodyOrHeadAtoms(bodyatoms, bodyatoms2, relationz);

			// Replace symbol _x with attribute name for every variable in the head atoms
			
			Atom[] headatoms = ic.getHeadAtoms();
			Atom[] headatoms2 = new Atom[headatoms.length];
			processDependencyBodyOrHeadAtoms(headatoms, headatoms2, relationz);
		
			Dependency ic2;
			if (ic instanceof TGD && ((TGD) ic).isGuarded()) {
				ic2 = LinearGuarded.create(bodyatoms2, headatoms2, ic.getName());
			} else {
				ic2 = TGD.create(bodyatoms2, headatoms2, ic.getName());
			}
			dependencys2[d] = ic2;
			
			TreeItem<String> ti = new TreeItem<>(ic2.getName().equals("dependency") ? ic2.toString() : ic2.getName(), imageView);
			dependencies.getChildren().add(ti);
		}
		
		// Swap the old schema with the new schema
		
		Schema schema = new Schema(relationz, dependencys2);
		ArrayList<ObservableSchema> obslist = new ArrayList<>();
		for(ObservableSchema obschema : this.schemas.values())
		{
			if(obschema.equals(s))
			{
				obslist.add(obschema);
			}
		}
		for(ObservableSchema obs : obslist)
		{
			this.schemas.remove(s.getName(), s);
			s.setSchema(schema);
			this.schemas.putIfAbsent(s.getName(), s);
		}
		map.clear();

		if (!relations.getChildren().isEmpty()) {
			item.getChildren().add(relations);
		}

		if (!services.getChildren().isEmpty()) {
			item.getChildren().add(services);
		}

		if (!views.getChildren().isEmpty()) {
			item.getChildren().add(views);
		}

		if (!dependencies.getChildren().isEmpty()) {
			item.getChildren().add(dependencies);
		}
	}

	/**
	 * List files.
	 *
	 * @param directory
	 *            the directory
	 * @param prefix
	 *            the prefix
	 * @param suffix
	 *            the suffix
	 * @return the file[]
	 */
	private static File[] listFiles(File directory, final String prefix, final String suffix) {
		Preconditions.checkArgument(directory.isDirectory(),
				"Invalid internal schema directory " + directory.getAbsolutePath());
		return directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix) && name.endsWith(suffix);
			}
		});
	}

	/**
	 * Update the plan/search views.
	 */
	void updateWidgets() {
		while (this.dataQueue != null && !this.dataQueue.isEmpty()) {
			Object o = this.dataQueue.poll();
			// ObservablePlan: Update the result table
			if (o instanceof ObservablePlan) {
				this.currentPlan.set((ObservablePlan) o);
				this.plansTableView.getItems().set(this.plansTableView.getSelectionModel().getSelectedIndex(),
						(ObservablePlan) o);
				this.plansTableView.getSelectionModel().select((ObservablePlan) o);
			}
			// ObservableSchema: run loadTreeItem or reloadTreeItem
			if (o instanceof ObservableSchema) {
				ObservableSchema s = this.schemas.get(((ObservableSchema) o).getName());
				TreeItem<String> item = null;
				if (s == null) {
					s = (ObservableSchema) o;
					item = this.loadTreeItem(s);
					this.schemas.put(s.getName(), s);
					this.queries.put(s.getName(), FXCollections.<ObservableQuery>observableArrayList());
				} else {
					for (TreeItem<String> child : this.schemasTreeView.getRoot().getChildren()) {
						if (child.getValue().equals(s.getName())) {
							item = child;
							this.reloadTreeItem(item, s);
							break;
						}
					}
				}
				this.saveSchema(s);
				this.schemaSelected(item);
			}
			// ObservableQuery: run saveQuery 
			if (o instanceof ObservableQuery) {
				ObservableQuery q = (ObservableQuery) o;
				this.currentQuery.set(q);
				this.queries.get(this.currentSchema.getValue().getName()).add(q);
				this.saveQuery(q);
				this.plans.put(Pair.of(this.currentSchema.get(), q),
						FXCollections.<ObservablePlan>observableArrayList());
			}
		}
	}

	/**
	 * Animation timer, required to update plan/search views from the main JavaFX
	 * thread.
	 */
	private void prepareTimeline() {
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				PDQController.this.updateWidgets();
			}
		}.start();
	}
}
