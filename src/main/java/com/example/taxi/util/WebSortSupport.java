package com.example.taxi.util;

import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

/**
 * Construction de {@link Sort} pour les listes MVC à partir d'un paramètre
 * {@code ?sort=propriete,direction} (ex : {@code rideDateTime,desc}).
 *
 * <p>Les propriétés sont filtrées par une liste blanche : sans cela, un nom de
 * champ inexistant ou une relation JPA serait transmis à la couche de
 * persistance, qui répondrait par une erreur 500.</p>
 */
public final class WebSortSupport {

    private WebSortSupport() {
    }

    /**
     * @param param      valeur brute du paramètre {@code sort}
     * @param fallback   champ utilisé si {@code param} est absent ou invalide
     * @param ascending  sens appliqué au champ de repli
     * @param allowed    propriétés triables autorisées
     * @return un {@link Sort} toujours valide
     */
    public static Sort toSort(String param, String fallback, boolean ascending, String... allowed) {
        Sort fallbackSort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, fallback);

        if (param == null || param.isBlank()) {
            return fallbackSort;
        }
        String[] parts = param.split(",", 2);
        String property = parts[0].trim();
        if (!isAllowed(property, allowed)) {
            return fallbackSort;
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    /**
     * Construit le paramètre {@code sort} pour un clic sur un en-tête de
     * colonne : un second clic sur la même colonne inverse le sens.
     */
    public static String toggle(String currentSort, String property) {
        if (isActive(currentSort, property)) {
            return property + "," + (isDescending(currentSort) ? "asc" : "desc");
        }
        return property + ",desc";
    }

    /** Vrai si {@code property} est la colonne actuellement triée. */
    public static boolean isActive(String currentSort, String property) {
        return currentSort != null
                && !currentSort.isBlank()
                && currentSort.split(",", 2)[0].trim().equals(property);
    }

    public static boolean isDescending(String currentSort) {
        if (currentSort == null || currentSort.isBlank()) {
            return false;
        }
        String[] parts = currentSort.split(",", 2);
        return parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
    }

    private static boolean isAllowed(String property, String... allowed) {
        return property != null && !property.isBlank()
                && Arrays.stream(allowed).anyMatch(property::equals);
    }

    /** Liste des propriétés triables d'une entité, pour les contrôleurs. */
    public static List<String> sortableFields(String... fields) {
        return List.of(fields);
    }
}
