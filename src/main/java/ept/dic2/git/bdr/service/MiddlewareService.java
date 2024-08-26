package ept.dic2.git.bdr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MiddlewareService {

    private final JdbcTemplate fragment1JdbcTemplate;
    private final JdbcTemplate fragment2JdbcTemplate;
    private final JdbcTemplate fragment1CopyJdbcTemplate;
    private final JdbcTemplate fragment2CopyJdbcTemplate;

    private static final String SELECT_QUERY = "SELECT";
    private static final String INSERT_QUERY = "INSERT";
    private static final String UPDATE_QUERY = "UPDATE";
    private static final String DELETE_QUERY = "DELETE";

    @Autowired
    public MiddlewareService(
            @Qualifier("fragment1JdbcTemplate") JdbcTemplate fragment1JdbcTemplate,
            @Qualifier("fragment2JdbcTemplate") JdbcTemplate fragment2JdbcTemplate,
            @Qualifier("fragment1CopyJdbcTemplate") JdbcTemplate fragment1CopyJdbcTemplate,
            @Qualifier("fragment2CopyJdbcTemplate") JdbcTemplate fragment2CopyJdbcTemplate) {

        this.fragment1JdbcTemplate = fragment1JdbcTemplate;
        this.fragment2JdbcTemplate = fragment2JdbcTemplate;
        this.fragment1CopyJdbcTemplate = fragment1CopyJdbcTemplate;
        this.fragment2CopyJdbcTemplate = fragment2CopyJdbcTemplate;
    }

    private static final Set<String> GLOBAL_FIELDS = new HashSet<>(Arrays.asList("voyage_id", "titre", "description", "classe", "prix", "date_depart", "date_retour", "destination", "nom_voyageur", "agence"));

    public List<Map<String, Object>> executeQuery(String query) {
        try {
            String fragment = determineFragment(query);
            String queryType = determineQueryType(query);

            return switch (queryType) {
                case SELECT_QUERY -> executeSelectQuery(query, fragment);
                case INSERT_QUERY -> executeInsertQuery(query, fragment);
                case UPDATE_QUERY -> executeUpdateQuery(query, fragment);
                case DELETE_QUERY -> executeDeleteQuery(query, fragment);
                default -> throw new IllegalArgumentException("Invalid query type specified.");
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + query, e);
        }
    }

    private String determineQueryType(String query) {
        query = query.trim().toUpperCase();
        if (query.startsWith(SELECT_QUERY)) {
            return SELECT_QUERY;
        } else if (query.startsWith(INSERT_QUERY)) {
            return INSERT_QUERY;
        } else if (query.startsWith(UPDATE_QUERY)) {
            return UPDATE_QUERY;
        } else if (query.startsWith(DELETE_QUERY)) {
            return DELETE_QUERY;
        } else {
            throw new IllegalArgumentException("Unsupported query type: " + query);
        }
    }

    private String determineFragment(String query) {
        String sanitizedQuery = query.toLowerCase().replaceAll("\\s+", " ");
        if (sanitizedQuery.startsWith("insert")) {
            String[] parts = sanitizedQuery.split("values");
            if (parts.length == 2) {
                String valuesPart = parts[1];
                String[] values = valuesPart.split(",");
                if (values.length >= 8) { // Position de la colonne "destination"
                    String destination = values[7].trim();
                    if (destination.equals("'fez'")) {
                        return "fragment1";
                    } else if (destination.equals("'mecque'")) {
                        return "fragment2";
                    } else {
                        throw new IllegalArgumentException("Invalid destination value: " + destination);
                    }
                }
            }
            throw new IllegalArgumentException("Invalid insertion query format: " + query);
        } else if (sanitizedQuery.startsWith("delete") || sanitizedQuery.startsWith("update")) {
            if (sanitizedQuery.contains("destination = 'fez'")) {
                return "fragment1";
            } else if (sanitizedQuery.contains("destination = 'mecque'")) {
                return "fragment2";
            } else if (sanitizedQuery.contains("destination =")) {
                throw new IllegalArgumentException("Invalid or unsupported destination value: " + query);
            } else {
                return "both";
            }
        } else if (GLOBAL_FIELDS.stream().anyMatch(sanitizedQuery::contains)) {
            if (sanitizedQuery.contains("destination = 'fez'")) {
                return "fragment1";
            } else if (sanitizedQuery.contains("destination = 'mecque'")) {
                return "fragment2";
            } else {
                return "both";
            }
        } else {
            return "both";
        }
    }


    private List<Map<String, Object>> executeSelectQuery(String query, String fragment) {
        try {
            if (fragment.equals("both")) {
                List<Map<String, Object>> results1 = executeQueryWithFallback(fragment1JdbcTemplate, query, fragment1CopyJdbcTemplate);
                List<Map<String, Object>> results2 = executeQueryWithFallback(fragment2JdbcTemplate, query, fragment2CopyJdbcTemplate);
                return mergeResults(results1, results2);
            } else {
                return executeQueryWithFallback(fragment.equals("fragment1") ? fragment1JdbcTemplate : fragment2JdbcTemplate, query,
                        fragment.equals("fragment1") ? fragment1CopyJdbcTemplate : fragment2CopyJdbcTemplate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SELECT query: " + query, e);
        }
    }

    private void executeTransactionalUpdate(String query, String fragment) {
        try {
            switch (fragment) {
                case "fragment1" -> {
                    fragment1JdbcTemplate.update(query);
                    fragment1CopyJdbcTemplate.update(query);
                }
                case "fragment2" -> {
                    fragment2JdbcTemplate.update(query);
                    fragment2CopyJdbcTemplate.update(query);
                }
                case "both" -> {
                    fragment1JdbcTemplate.update(query);
                    fragment1CopyJdbcTemplate.update(query);
                    fragment2JdbcTemplate.update(query);
                    fragment2CopyJdbcTemplate.update(query);
                }
                default -> throw new IllegalArgumentException("Invalid fragment specified.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + query, e);
        }
    }

    private List<Map<String, Object>> executeInsertQuery(String query, String fragment) {
        try {
            executeTransactionalUpdate(query, fragment);
            Map<String, Object> successMessage = new HashMap<>();
            successMessage.put("message", "Voyage ajouté avec succès");
            return Collections.singletonList(successMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute INSERT query: " + query, e);
        }
    }

    private List<Map<String, Object>> executeUpdateQuery(String query, String fragment) {
        try {
            executeTransactionalUpdate(query, fragment);
            Map<String, Object> successMessage = new HashMap<>();
            successMessage.put("message", "Voyage mis à jour avec succès");
            return Collections.singletonList(successMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute UPDATE query: " + query, e);
        }
    }

    private List<Map<String, Object>> executeDeleteQuery(String query, String fragment) {
        try {
            executeTransactionalUpdate(query, fragment);
            Map<String, Object> successMessage = new HashMap<>();
            successMessage.put("message", "Voyage supprimé avec succès");
            return Collections.singletonList(successMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DELETE query + query: " + query, e);
        }
    }

    private List<Map<String, Object>> mergeResults(List<Map<String, Object>> results1, List<Map<String, Object>> results2) {
        List<Map<String, Object>> mergedResults = new ArrayList<>(results1);
        mergedResults.addAll(results2);
        // Vérifier si le paramètre "voyage_id" est présent dans les résultats
        boolean hasVoyageId = mergedResults.stream().anyMatch(m -> m.containsKey("voyage_id"));
        // Ordonner les résultats uniquement si "voyage_id" est présent dans les résultats
        if (hasVoyageId) {
            mergedResults.sort(Comparator.comparing(m -> (int) m.get("voyage_id")));
        }
        return mergedResults;
    }

    private List<Map<String, Object>> executeQueryWithFallback(JdbcTemplate primaryJdbcTemplate, String query, JdbcTemplate fallbackJdbcTemplate) {
        try {
            return primaryJdbcTemplate.queryForList(query);
        } catch (Exception e) {
            try {
                return fallbackJdbcTemplate.queryForList(query);
            } catch (Exception ex) {
                throw new RuntimeException("Both primary and fallback queries failed for query: " + query, ex);
            }
        }
    }
}
