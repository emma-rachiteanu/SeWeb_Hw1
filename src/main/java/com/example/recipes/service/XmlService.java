package com.example.recipes.service;

import com.example.recipes.model.Recipe;
import com.example.recipes.model.UserProfile;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.*;

@Service
public class XmlService {

    private final Path xmlPath = Paths.get("src", "main", "resources", "data", "recipes.xml");
    private final Path xsdPath = Paths.get("src", "main", "resources", "data", "recipes.xsd");
    private final Path xslPath = Paths.get("src", "main", "resources", "xslt", "recipes.xsl");

    private final List<String> difficulties = List.of("Beginner", "Intermediate", "Advanced");

    private final List<String> cuisines = List.of(
            "Italian", "Asian", "Mexican", "French", "Greek", "Indian",
            "American", "Romanian", "European", "Middle Eastern",
            "Chinese", "Japanese", "Spanish", "British"
    );

    public List<String> getDifficulties() {
        return difficulties;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public List<Recipe> getAllRecipes() {
        try {
            Document document = loadXml();
            NodeList nodes = document.getElementsByTagName("recipe");
            return convertNodesToRecipes(nodes);
        } catch (Exception e) {
            throw new RuntimeException("Could not read recipes from XML.", e);
        }
    }

    public List<UserProfile> getAllUsers() {
        try {
            Document document = loadXml();
            NodeList nodes = document.getElementsByTagName("user");

            List<UserProfile> users = new ArrayList<>();

            for (int i = 0; i < nodes.getLength(); i++) {
                Element userElement = (Element) nodes.item(i);

                users.add(new UserProfile(
                        userElement.getAttribute("id"),
                        getText(userElement, "name"),
                        getText(userElement, "surname"),
                        getText(userElement, "skillLevel"),
                        getText(userElement, "preferredCuisine")
                ));
            }

            return users;
        } catch (Exception e) {
            throw new RuntimeException("Could not read users from XML.", e);
        }
    }

    public UserProfile findUserById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        try {
            Document document = loadXml();

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            String expression = "/recipeApp/users/user[@id = " + xpathLiteral(id) + "]";

            Node node = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

            if (node == null) {
                return null;
            }

            Element userElement = (Element) node;

            return new UserProfile(
                    userElement.getAttribute("id"),
                    getText(userElement, "name"),
                    getText(userElement, "surname"),
                    getText(userElement, "skillLevel"),
                    getText(userElement, "preferredCuisine")
            );

        } catch (Exception e) {
            throw new RuntimeException("Could not find user by ID.", e);
        }
    }

    public UserProfile getFirstUser() {
        List<UserProfile> users = getAllUsers();

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }

    public List<Recipe> recommendBySkill() {
        String expression = "/recipeApp/recipes/recipe[difficulty = /recipeApp/users/user[1]/skillLevel]";
        return findRecipesByXPath(expression);
    }

    public List<Recipe> recommendBySkill(String skillLevel) {
        if (!difficulties.contains(skillLevel)) {
            throw new IllegalArgumentException("Invalid skill level.");
        }

        String expression = "/recipeApp/recipes/recipe[difficulty = " + xpathLiteral(skillLevel) + "]";
        return findRecipesByXPath(expression);
    }

    public List<Recipe> recommendByUserSkill(String userId) {
        UserProfile user = findUserById(userId);

        if (user == null) {
            return recommendBySkill();
        }

        String expression = "/recipeApp/recipes/recipe[difficulty = /recipeApp/users/user[@id = "
                + xpathLiteral(user.getId()) + "]/skillLevel]";

        return findRecipesByXPath(expression);
    }

    public List<Recipe> recommendBySkillAndCuisine() {
        String expression = "/recipeApp/recipes/recipe[" +
                "difficulty = /recipeApp/users/user[1]/skillLevel " +
                "and cuisines/cuisine = /recipeApp/users/user[1]/preferredCuisine" +
                "]";
        return findRecipesByXPath(expression);
    }

    public List<Recipe> recommendBySkillAndCuisineForUser(String userId) {
        UserProfile user = findUserById(userId);

        if (user == null) {
            return recommendBySkillAndCuisine();
        }

        String expression = "/recipeApp/recipes/recipe[" +
                "difficulty = /recipeApp/users/user[@id = " + xpathLiteral(user.getId()) + "]/skillLevel " +
                "and cuisines/cuisine = /recipeApp/users/user[@id = " + xpathLiteral(user.getId()) + "]/preferredCuisine" +
                "]";

        return findRecipesByXPath(expression);
    }

    public List<Recipe> findRecipesByCuisine(String cuisine) {
        String expression = "/recipeApp/recipes/recipe[cuisines/cuisine = " + xpathLiteral(cuisine) + "]";
        return findRecipesByXPath(expression);
    }

    public Recipe findRecipeById(String id) {
        String expression = "/recipeApp/recipes/recipe[@id = " + xpathLiteral(id) + "]";

        List<Recipe> results = findRecipesByXPath(expression);

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    public void addRecipe(String title, String cuisine1, String cuisine2, String difficulty) {
        validateRecipeInput(title, cuisine1, cuisine2, difficulty);

        try {
            Document document = loadXml();

            Element recipesElement = (Element) document.getElementsByTagName("recipes").item(0);

            Element recipeElement = document.createElement("recipe");
            recipeElement.setAttribute("id", generateNextRecipeId(document));

            Element titleElement = document.createElement("title");
            titleElement.setTextContent(title.trim());

            Element cuisinesElement = document.createElement("cuisines");

            Element cuisine1Element = document.createElement("cuisine");
            cuisine1Element.setTextContent(cuisine1);

            Element cuisine2Element = document.createElement("cuisine");
            cuisine2Element.setTextContent(cuisine2);

            Element difficultyElement = document.createElement("difficulty");
            difficultyElement.setTextContent(difficulty);

            cuisinesElement.appendChild(cuisine1Element);
            cuisinesElement.appendChild(cuisine2Element);

            recipeElement.appendChild(titleElement);
            recipeElement.appendChild(cuisinesElement);
            recipeElement.appendChild(difficultyElement);

            recipesElement.appendChild(recipeElement);

            saveXml(document);
            validateXml();

        } catch (Exception e) {
            throw new RuntimeException("Could not add recipe.", e);
        }
    }

    public void addUser(String name, String surname, String skillLevel, String preferredCuisine) {
        validateUserInput(name, surname, skillLevel, preferredCuisine);

        try {
            Document document = loadXml();

            Element usersElement = (Element) document.getElementsByTagName("users").item(0);

            Element userElement = document.createElement("user");
            userElement.setAttribute("id", generateNextUserId(document));

            Element nameElement = document.createElement("name");
            nameElement.setTextContent(name.trim());

            Element surnameElement = document.createElement("surname");
            surnameElement.setTextContent(surname.trim());

            Element skillElement = document.createElement("skillLevel");
            skillElement.setTextContent(skillLevel);

            Element cuisineElement = document.createElement("preferredCuisine");
            cuisineElement.setTextContent(preferredCuisine);

            userElement.appendChild(nameElement);
            userElement.appendChild(surnameElement);
            userElement.appendChild(skillElement);
            userElement.appendChild(cuisineElement);

            usersElement.appendChild(userElement);

            saveXml(document);
            validateXml();

        } catch (Exception e) {
            throw new RuntimeException("Could not add user.", e);
        }
    }

    public String transformRecipesWithXsl() {
        UserProfile firstUser = getFirstUser();

        if (firstUser == null) {
            return "";
        }

        return transformRecipesWithXsl(firstUser.getId());
    }

    public String transformRecipesWithXsl(String selectedUserId) {
        try {
            UserProfile selectedUser = findUserById(selectedUserId);

            if (selectedUser == null) {
                selectedUser = getFirstUser();
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xslPath.toFile()));

            if (selectedUser != null) {
                transformer.setParameter("selectedUserId", selectedUser.getId());
            }

            StringWriter writer = new StringWriter();

            transformer.transform(
                    new StreamSource(xmlPath.toFile()),
                    new StreamResult(writer)
            );

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Could not transform XML using XSL.", e);
        }
    }

    private List<Recipe> findRecipesByXPath(String expression) {
        try {
            Document document = loadXml();

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            return convertNodesToRecipes(nodes);

        } catch (Exception e) {
            throw new RuntimeException("Could not run XPath query.", e);
        }
    }

    private List<Recipe> convertNodesToRecipes(NodeList nodes) {
        List<Recipe> recipes = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element recipeElement = (Element) nodes.item(i);

            NodeList cuisineNodes = recipeElement.getElementsByTagName("cuisine");

            recipes.add(new Recipe(
                    recipeElement.getAttribute("id"),
                    getText(recipeElement, "title"),
                    cuisineNodes.item(0).getTextContent(),
                    cuisineNodes.item(1).getTextContent(),
                    getText(recipeElement, "difficulty")
            ));
        }

        return recipes;
    }

    private Document loadXml() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlPath.toFile());

        document.getDocumentElement().normalize();

        return document;
    }

    private void saveXml(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(
                new DOMSource(document),
                new StreamResult(xmlPath.toFile())
        );
    }

    private void validateXml() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdPath.toFile());

            schema.newValidator().validate(new StreamSource(xmlPath.toFile()));

        } catch (Exception e) {
            throw new RuntimeException("XML file is not valid according to the XSD schema.", e);
        }
    }

    private String getText(Element parent, String tagName) {
        return parent.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private String generateNextRecipeId(Document document) {
        NodeList recipes = document.getElementsByTagName("recipe");
        return "r" + (recipes.getLength() + 1);
    }

    private String generateNextUserId(Document document) {
        NodeList users = document.getElementsByTagName("user");
        return "u" + (users.getLength() + 1);
    }

    private void validateRecipeInput(String title, String cuisine1, String cuisine2, String difficulty) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title is required.");
        }

        if (!cuisines.contains(cuisine1) || !cuisines.contains(cuisine2)) {
            throw new IllegalArgumentException("Invalid cuisine type.");
        }

        if (cuisine1.equals(cuisine2)) {
            throw new IllegalArgumentException("The two cuisine types must be different.");
        }

        if (!difficulties.contains(difficulty)) {
            throw new IllegalArgumentException("Invalid difficulty level.");
        }
    }

    private void validateUserInput(String name, String surname, String skillLevel, String preferredCuisine) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (surname == null || surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Surname is required.");
        }

        if (!difficulties.contains(skillLevel)) {
            throw new IllegalArgumentException("Invalid skill level.");
        }

        if (!cuisines.contains(preferredCuisine)) {
            throw new IllegalArgumentException("Invalid preferred cuisine.");
        }
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        throw new IllegalArgumentException("Invalid XPath value.");
    }
}