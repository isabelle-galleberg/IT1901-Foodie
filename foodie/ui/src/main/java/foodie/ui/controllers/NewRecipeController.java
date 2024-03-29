package foodie.ui.controllers;

import foodie.core.Cookbook;
import foodie.core.Ingredient;
import foodie.core.Recipe;
import foodie.ui.FxmlModel;
import foodie.ui.SceneHandler;
import foodie.ui.SceneName;
import foodie.ui.data.CookbookAccess;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * Controller for page responsible for creating and editing recipes.
 */
public class NewRecipeController extends AbstractController {


  private Recipe newRecipe;
  private Cookbook cookbook = new Cookbook();
  private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
  private boolean editing = false;
  private String recipeName;
  private ToggleGroup group;


  @FXML
  private TextField ingredientTitle;

  @FXML
  private TextField ingredientAmount;

  @FXML
  private ChoiceBox<String> ingredientUnit;

  @FXML
  private TextField recipePortions;

  @FXML
  private TextField recipeTitle;

  @FXML
  private ListView<Ingredient> ingredientListView;

  @FXML
  private TextArea recipeDescription;

  @FXML
  private Label errorMessageLabel;

  @FXML
  private Button backButton;
  @FXML
  private ToggleButton breakfast;
  @FXML
  private ToggleButton lunch;
  @FXML
  private ToggleButton dinner;
  @FXML
  private ToggleButton dessert;

  @FXML
  private Button saveRecipeButton;
  @FXML
  private Button deleteRecipeButton;
  @FXML
  private Button createRecipeButton;
  @FXML
  private Button deleteIngredientButton;
  @FXML
  private Button editIngredientButton;
  @FXML
  private AnchorPane page;
  @FXML
  private Label titleLabel;

  @FXML
  private HBox hb;


  /**
   * Validates input while typing, gives the field a red frame if invalid input is typed.
   *
   * @param k the input typed
   */
  public void numLetValidate(KeyEvent k) {
    TextField source = (TextField) k.getSource();
    if (!source.getText().matches("^[ÆØÅæøåa-zA-Z0-9\\s]+$") && !source.getText().isEmpty()) {
      errorMessageLabel.setText("Must be letters or numbers");
      source.getStyleClass().add("text-field-red");
    } else {
      errorMessageLabel.setText("");
      source.getStyleClass().setAll("text-field");
    }
  }

  /**
   * Validates integer-input while typing, gives the field a red frame if invalid input is typed.
   *
   * @param k the input typed
   */
  public void intValidate(KeyEvent k) {
    TextField source = (TextField) k.getSource();
    if (source.getText().isEmpty()) {
      source.getStyleClass().setAll("text-field");
      // source.setStyle("-fx-border-color: BLACK; -fx-background-color: WHITE");
      return;
    }
    try {
      Integer.parseInt(source.getText());
      errorMessageLabel.setText("");
      source.getStyleClass().setAll("text-field");
    } catch (Exception e) {
      errorMessageLabel.setText("Must be a integer");
      source.getStyleClass().add("text-field-red");
    }
  }

  /**
   * Validates double-input while typing, gives the field a red frame if invalid input is typed.
   *
   * @param k the input typed
   */
  public void doubleValidate(KeyEvent k) {
    TextField source = (TextField) k.getSource();
    if (source.getText().isEmpty()) {
      source.getStyleClass().setAll("text-field");
      return;
    }
    try {
      Double.parseDouble(source.getText());
      errorMessageLabel.setText("");
      source.getStyleClass().setAll("text-field");
    } catch (Exception e) {
      errorMessageLabel.setText("Must be a decimal");
      source.getStyleClass().add("text-field-red");
    }
  }

  /**
   * Adds ingredient to recipe.
   *
   * @param ae when save ingredient-button is clicked
   * 
   * @throws IllegalArgumentException if ingredient-title is not set
   */
  public void addIngredientButton(ActionEvent ae) {
    try {
      if (ingredientAmount.getText() != null && !ingredientAmount.getText().isEmpty()) {
        Ingredient newIngredient = new Ingredient(ingredientTitle.getText(),
            Double.parseDouble(ingredientAmount.getText()), ingredientUnit.getValue().toString());
        ingredients.add(newIngredient);
      } else {
        Ingredient newIngredient = new Ingredient(ingredientTitle.getText());
        ingredients.add(newIngredient);
      }

      ingredientAmount.clear();
      ingredientTitle.clear();
      ingredientUnit.setValue(null);;

    } catch (NumberFormatException e) {
      errorMessageLabel.setText("Invalid input: ingredient amount must be a number");
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      errorMessageLabel.setText("Invalid Ingredient name");
      e.printStackTrace();
    } catch (NullPointerException e) {
      errorMessageLabel.setText("You have missing fields");
      e.printStackTrace();
    }
  }

  @FXML
  protected void handleDeleteIngredient() {
    Ingredient ing = ingredientListView.getSelectionModel().getSelectedItem();
    if (ing != null) {
      ingredients.remove(ing);
    }
  }

  @FXML
  protected void handleEditIngredient() {
    Ingredient ing = ingredientListView.getSelectionModel().getSelectedItem();
    if (ing != null) {
      ingredients.remove(ing);
      ingredientAmount.setText(Double.toString(ing.getAmount()));
      ingredientTitle.setText(ing.getName());
      ingredientUnit.setValue(ing.getUnit());
    }
  }

  /**
   * Creates a new recipe when create new recipe-button is pushed and saves it.
   *
   * @param ae when create new recipe-button is pushed
   * 
   */
  public void createRecipeButtonPushed(ActionEvent ae) throws IOException {
    try {
      System.out.println(dataAccess.toString());
      dataAccess.addRecipe(createRecipe());
      backButton.fire();

    } catch (NullPointerException e) {
      errorMessageLabel.setText("You have empty fields");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("You have illegal input");
    }
  }

  /**
   * Saves edited recipe to server.
   */
  @FXML
  protected void saveRecipe() {
    try {
      newRecipe = createRecipe();
      dataAccess.editRecipe(recipeName, newRecipe);
      setSelectedRecipe(newRecipe);
      backButton.fire();

    } catch (Exception e) {
      errorMessageLabel.setText(e.getMessage());
      System.out.println(e.getMessage());

    }
  }

  /**
   * Creates edited recipe.
   */
  private Recipe createRecipe() {
    if (!editing) {
      if ((this.cookbook).isInCookbook(recipeTitle.getText())) {
        (this.recipeTitle).setText("");
        throw new IllegalArgumentException("This recipe title already exists");
      }
    }
    this.newRecipe = new Recipe(recipeTitle.getText());
    if (!recipePortions.getText().isEmpty()) {
      newRecipe.setPortions(Integer.parseInt(recipePortions.getText()));
    }
    if (!(recipeDescription.getText() == null)) {
      this.newRecipe.setDescription(recipeDescription.getText());
    }
    for (Ingredient i : ingredients) {
      this.newRecipe.addIngredient(i);
    }
    if (group.getSelectedToggle() != null) {
      this.newRecipe.setLabel((String) group.getSelectedToggle().getUserData());
    } else {
      this.newRecipe.removeLabel();
    }
    this.newRecipe.setDescription(recipeDescription.getText());
    return newRecipe;
  }

  /**
   * Initializes data from another scene.
   *
   * @param recipe the recipe to initialize
   * 
   */
  protected void initData(Recipe recipe) {
    this.recipeTitle.setText(recipe.getName());
    if (recipe.getPortions() != 0) {
      this.recipePortions.setText(String.valueOf(recipe.getPortions()));
    }
    this.recipeName = recipe.getName();
    if (!recipe.getDescription().isEmpty()) {
      this.recipeDescription.setText(recipe.getDescription());
    }
    if (!recipe.getLabel().isEmpty()) {
      switch (recipe.getLabel()) {
        case "breakfast":
          group.selectToggle(breakfast);
          break;
        case "lunch":
          group.selectToggle(lunch);
          break;
        case "dinner":
          group.selectToggle(dinner);
          break;
        case "dessert":
          group.selectToggle(dessert);
          break;
        default:
          break;
      }
    }
    ingredients.setAll(recipe.getIngredients());
    this.editing = true;

    createRecipeButton.setVisible(false);
    saveRecipeButton.setVisible(true);
    deleteRecipeButton.setVisible(true);
  }


  /**
   * Clears all text-field and sets sets visibility of buttons to fit the new-recipe page.
   *
   */
  protected void clear() {
    clearTextFields();
    ingredients.clear();
    this.editing = false;
    if (group.getSelectedToggle() != null) {
      group.getSelectedToggle().setSelected(false);
    }
    
    createRecipeButton.setVisible(true);
    saveRecipeButton.setVisible(false);
    deleteRecipeButton.setVisible(true);
  }

  /**
   * Deletes recipe from dataAccess and change scene to main-page.
   *
   */
  @FXML
  public void deleteRecipe() {
    dataAccess.deleteRecipe(selectedRecipe.getName());
    setBackButtonTarget(SceneHandler.getScenes().get(SceneName.MAIN));
    backButton.fire();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ingredientListView.setCellFactory(listView -> {
      IngredientListCell listCell = new IngredientListCell();
      return listCell;
    });
    ingredientListView.getStyleClass().add("ingredientListCell");
    deleteIngredientButton.setDisable(true);
    editIngredientButton.setDisable(true);
    setListViewListener();
    setBackButtonTarget(SceneHandler.getScenes().get(SceneName.MAIN));
    setToggleGroup();
    ingredientListView.setItems(ingredients);
    update();
  }

  private void setToggleGroup() {
    ingredientUnit.getItems().addAll(Ingredient.units);
    this.group = new ToggleGroup();
    breakfast.setToggleGroup(group);
    breakfast.setUserData("breakfast");
    lunch.setToggleGroup(group);
    lunch.setUserData("lunch");
    dinner.setToggleGroup(group);
    dinner.setUserData("dinner");
    dessert.setToggleGroup(group);
    dessert.setUserData("dessert");
  }

  /**
   * Sets the SceneTarget for return button.
   */
  public void setBackButtonTarget(FxmlModel model) {
    backButton.setOnAction(ea -> {
      changeScene(model);
    });
  }

  @Override
  public void update() {
    if (getSelectedrecipe() != null) {
      titleLabel.setText("Edit your recipe!");
      clearTextFields();
      initData(selectedRecipe);
      setBackButtonTarget(SceneHandler.getScenes().get(SceneName.VIEWRECIPE));

    } else {
      titleLabel.setText("Create a new recipe!");
      clear();
      setBackButtonTarget(SceneHandler.getScenes().get(SceneName.MAIN));
    }
  }


  /**
   * Listener to open a Recipe from ListView when selected.
   *
   */
  public void setListViewListener() {
    ingredientListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Ingredient>() {
      @Override
      public void changed(ObservableValue<? extends Ingredient> observable, Ingredient oldValue, Ingredient newValue) {
        if (newValue != null) {
          deleteIngredientButton.setDisable(false);
          editIngredientButton.setDisable(false);
        } else {
          deleteIngredientButton.setDisable(true);
          editIngredientButton.setDisable(true);
        }
      }
    });
    page.setOnMouseClicked(new EventHandler<Event>() {
      @Override
      public void handle(Event event) {
        ingredientListView.getSelectionModel().clearSelection();
      }
    });
  }

  
  /**
   * Clears all text-field.
   */
  public void clearTextFields() {
    ingredientAmount.clear();
    ingredientTitle.clear();
    ingredientUnit.setValue(null);
    this.recipeTitle.clear();;
    this.recipePortions.clear();;
    this.recipeName = "";
    this.recipeDescription.clear();
  }


  public void setDataAccess(CookbookAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  protected List<Ingredient> getIngredients() {
    return new ArrayList<Ingredient>(ingredients);
  }
}
