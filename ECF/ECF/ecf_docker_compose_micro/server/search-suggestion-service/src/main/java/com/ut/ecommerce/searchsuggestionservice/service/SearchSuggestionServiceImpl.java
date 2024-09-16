package com.ut.ecommerce.searchsuggestionservice.service;

import com.ut.ecommerce.searchsuggestionservice.dto.SearchSuggestionKeywordInfo;
import com.ut.ecommerce.searchsuggestionservice.util.Permutation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class SearchSuggestionServiceImpl implements SearchSuggestionService {
   

    private static final Logger logger = LogManager.getLogger(SearchSuggestionServiceImpl.class);

 // Map pour stocker les mots-clés de recherche par préfixe
    HashMap<String, List<SearchSuggestionKeywordInfo>> prefixKeywordsMap = new HashMap<>();

    // Liste pour stocker les suggestions de recherche par défaut
    List<SearchSuggestionKeywordInfo> defaultSearchSuggestionList = new LinkedList<>();

    // Méthode privée pour convertir une chaîne JSON en objet JSON
    private JSONObject parseJSONObject(String json) throws JSONException {
        return new JSONObject(json);
    }

    // Méthode privée pour extraire un tableau JSON d'un objet JSON donné une clé
    private JSONArray parseJSONArray(JSONObject jsonObject, String key) throws JSONException {
        return jsonObject.getJSONArray(key);
    }

    // Méthode privée pour ajouter des mots-clés à la Map en fonction d'un objet JSON avec une clé spécifiée
    private void addJsonObjKeywordToMap(JSONObject jsonResponse, String key, String attributeName) throws JSONException {
        JSONArray jsonArray = parseJSONArray(jsonResponse, key);

        for (int index = 0; index < jsonArray.length(); ++index) {
            JSONObject jsonObject = new JSONObject(jsonArray.get(index).toString());
            StringBuilder filterLink = new StringBuilder();
            // Ajouter les mots-clés à la Map
            addSearchSuggestionKeywords(jsonObject.getString("type"),
                    filterLink.append(attributeName).append("=").append(jsonObject.getString("id")));
            // Ajouter les suggestions de recherche par défaut pour certains attributs
            if (index < 10 && attributeName.equals("apparels")) {
                defaultSearchSuggestionList.add(new SearchSuggestionKeywordInfo(jsonObject.getString("type"),
                        filterLink, 1));
            }
        }
    }

    // Méthode privée pour ajouter des mots-clés à la Map à partir d'un tableau JSON avec une clé spécifiée
    private void addKeywordToMap(JSONObject jsonResponse, String key) throws JSONException {
        JSONArray jsonArray = parseJSONArray(jsonResponse, key);

        for (int index = 0; index < jsonArray.length(); ++index) {
            StringBuilder filterLink = new StringBuilder();
            // Ajouter les mots-clés à la Map
            addSearchSuggestionKeywords(jsonArray.get(index).toString(),
                    filterLink.append("productname=").append(jsonArray.get(index).toString()));
        }
    }

    // Méthode privée pour construire et ajouter des combinaisons de mots-clés à partir d'un tableau JSON avec une clé spécifiée et des noms d'attributs
    private void constructAndAddKeywordCombination(JSONObject jsonResponse, String key, String[] attributeNames) throws JSONException {
        System.out.println("constructAndAddKeywordCombination => jsonResponse : " + jsonResponse + "key : " + key + "attributeNames " + attributeNames);
        JSONArray jsonArray = parseJSONArray(jsonResponse, key);
        System.out.println("jsonArray = " + jsonArray);
        for (int index = 0; index < jsonArray.length(); ++index) {
            JSONObject jsonObject = new JSONObject(jsonArray.get(index).toString());
            int noOfAttributes = attributeNames.length;
            String[] keywords = new String[noOfAttributes];
            StringBuilder filterLink = new StringBuilder();
            for (int attrIndex = 1; attrIndex <= noOfAttributes; ++attrIndex) {
                keywords[attrIndex - 1] = jsonObject.getString(String.format("attr%d_type", attrIndex));
                filterLink.append(attributeNames[attrIndex - 1]).append("=")
                        .append(jsonObject.getString(String.format("attr%d_id", attrIndex))).append("::");
            }

            // Correction de la fin de la chaîne de liaison
            if(filterLink.charAt(filterLink.length() - 1) == ':') {
                filterLink.setLength(filterLink.length() - 2);
            }

            // Permutation des mots-clés et ajout à la Map
            Permutation permutation = new Permutation(keywords);
            for (String keyword : permutation.getOutput()) {
                addSearchSuggestionKeywords(keyword, filterLink);
            }
        }
    }

    // Méthode privée pour ajouter des mots-clés à la Map de suggestions de recherche
    private void addSearchSuggestionKeywords(String keyword, StringBuilder link) {
      //  System.out.println("addSearchSuggestionKeywords => keyword : " + keyword + "link : " + link);
        for (int index = 0; index < keyword.length(); ++index) {
            String prefix = keyword.substring(0, index + 1).toLowerCase();
            if (!prefixKeywordsMap.containsKey(prefix)) {
                prefixKeywordsMap.put(prefix, new ArrayList<>(Arrays.asList(
                        new SearchSuggestionKeywordInfo(keyword, link, 1))));
            } else {
                List<SearchSuggestionKeywordInfo> keywordList = prefixKeywordsMap.get(prefix);
                keywordList.add(new SearchSuggestionKeywordInfo(keyword, link, 1));
            }
        }
    }

    // Méthode pour charger les suggestions de recherche à partir d'une source externe
    public void loadSearchSuggestionToMap() {
        String URL = System.getenv("COMMON_DATA_SERVICE_URL") + "/search-suggestion-list";

        while (true) {
            try {
                System.out.println("URL = " + URL);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .header("accept", "application/json")
                        .uri(URI.create(URL))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    System.out.println("RESPONSE = " + response);

                    // Lire la réponse JSON et l'afficher
                    JSONObject jsonResponse = parseJSONObject(response.body());

                    System.out.println("jsonResponse = " + jsonResponse);

                    // Ajouter les mots-clés de différents types à la Map
                    addJsonObjKeywordToMap(jsonResponse, "genderKeywords", "genders");
                    addJsonObjKeywordToMap(jsonResponse, "brandKeywords", "brands");
                    addJsonObjKeywordToMap(jsonResponse, "apparelKeywords", "apparels");
                    addKeywordToMap(jsonResponse, "productKeywords");

                    // Ajouter les combinaisons de mots-clés à la Map
                    constructAndAddKeywordCombination(jsonResponse, "genderApparelKeywords",
                            new String[]{"genders", "apparels"});
                    constructAndAddKeywordCombination(jsonResponse, "genderBrandKeywords",
                            new String[]{"genders", "brands"});
                    constructAndAddKeywordCombination(jsonResponse, "apparelBrandKeywords",
                            new String[]{"apparels", "brands"});
                    constructAndAddKeywordCombination(jsonResponse, "threeAttrKeywords",
                            new String[]{"genders", "apparels", "brands"});

                    System.out.println("prefixKeywordsMap = " + prefixKeywordsMap.size());

                    // Si la Map est remplie, sortir de la boucle
                    if(prefixKeywordsMap.size() > 0) {
                        break;
                    }

                } else {
                    System.out.println("Erreur : Impossible de se connecter à l'API de suggestion de recherche, code d'état = "
                            + response.statusCode());
                }
            } catch (IOException | InterruptedException ioException) {
                System.out.println("Erreur : Impossible de se connecter à l'API de suggestion de recherche. Le serveur est peut-être hors ligne");
                System.out.println("Erreur : " + ioException);
            } catch (JSONException jsonException) {
                System.out.println("Erreur : Impossible de parser JSON pour l'API de suggestion de recherche");
                System.out.println("Erreur : " + jsonException);
                return;
            } catch (Exception e) {
                System.out.println("Erreur : Quelque chose s'est mal passé. Problème inconnu.");
                System.out.println("Erreur : " + e);
                return;
            }

            try {
                Thread.sleep(5000); // Attendre 5 secondes avant de réessayer
                System.out.println("Nouvelle tentative de connexion dans 5 secondes....");
            } catch (InterruptedException e) {
                System.out.println("Erreur : Impossible de mettre en pause");
            }
        }
    }

    // Méthode pour rechercher des mots-clés à partir de la Map de suggestions de recherche
    public List<SearchSuggestionKeywordInfo> searchKeywordFromMap(String q) {
        List<SearchSuggestionKeywordInfo> resultList = null;
        for (int index = q.length(); index > 0; --index) {
            String prefix = q.substring(0, index).toLowerCase();
            System.out.println("searchKeywordFromMap prefix : " + prefix);
            if (prefixKeywordsMap.containsKey(prefix)) {
                resultList = prefixKeywordsMap.get(prefix);
                break;
            }
        }

        return resultList;
    }

    // Méthode pour obtenir les suggestions de recherche par défaut
    public List<SearchSuggestionKeywordInfo> getDefaultSearchSuggestions() {
        return defaultSearchSuggestionList;
    }
}
