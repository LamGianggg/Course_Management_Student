package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.dto.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified DAO helper for:
 * 1. Query / Search (JPA Specifications, dynamic criteria, operators, joins, type conversions)
 * 2. Pagination (Pageable, bounds validation, page/size)
 * 3. Sorting (Sort, Sort.Order, ASC/DESC validation, multiple fields, fallback defaults)
 * 4. Combined Execution (Search + Pagination + Sorting in a single unified flow)
 *
 * Can be used statically across any entity or instantiated as HibernateDao<T>.
 */
public class HibernateDao<T> {

    // =========================================================================
    // CONSTANTS
    // =========================================================================

    // Pagination constants
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    // Sorting defaults
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;

    // Query defaults & pattern
    public static final Set<String> DEFAULT_ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "dob", "department.name"
    );

    /**
     * Pattern to match search criteria: field{operator}value
     * Operators: : (equals/contains), > (greater than), < (less than), ! (not equals)
     */
    private static final Pattern CRITERIA_PATTERN = Pattern.compile(
            "([\\w.]+?)([:<>!])(.+)"
    );

    // =========================================================================
    // INSTANCE FIELDS (Optional for HibernateDao<T> usage)
    // =========================================================================

    private JpaSpecificationExecutor<T> repository;
    private Set<String> allowedSearchFields;
    private Set<String> allowedSortFields;
    private String defaultSortField = DEFAULT_SORT_FIELD;
    private Sort.Direction defaultSortDirection = DEFAULT_SORT_DIRECTION;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public HibernateDao() {
    }

    public HibernateDao(JpaSpecificationExecutor<T> repository) {
        this.repository = repository;
    }

    public HibernateDao(JpaSpecificationExecutor<T> repository,
                        Set<String> allowedSearchFields,
                        Set<String> allowedSortFields) {
        this.repository = repository;
        this.allowedSearchFields = allowedSearchFields;
        this.allowedSortFields = allowedSortFields;
    }

    public HibernateDao(JpaSpecificationExecutor<T> repository,
                        Set<String> allowedSearchFields,
                        Set<String> allowedSortFields,
                        String defaultSortField,
                        Sort.Direction defaultSortDirection) {
        this.repository = repository;
        this.allowedSearchFields = allowedSearchFields;
        this.allowedSortFields = allowedSortFields;
        this.defaultSortField = defaultSortField != null ? defaultSortField : DEFAULT_SORT_FIELD;
        this.defaultSortDirection = defaultSortDirection != null ? defaultSortDirection : DEFAULT_SORT_DIRECTION;
    }

    // =========================================================================
    // 1. QUERY / SEARCH LOGIC
    // =========================================================================

    /**
     * Parse search query string and build a combined Specification with field whitelist validation.
     *
     * @param search        search query string (e.g. "name:john,dob>2003-01-01")
     * @param allowedFields set of allowed search fields (optional, if null or empty, no restriction)
     * @param <E>           entity type
     * @return combined Specification, or null if search is empty/null
     * @throws IllegalArgumentException if syntax is invalid or field is not allowed
     */
    public static <E> Specification<E> parse(String search, Set<String> allowedFields) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        List<SearchCriteria> criteriaList = new ArrayList<>();
        String[] conditions = search.split(",");

        for (String condition : conditions) {
            String trimmed = condition.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            Matcher matcher = CRITERIA_PATTERN.matcher(trimmed);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "Invalid search syntax: '" + trimmed
                                + "'. Expected format: field{operator}value "
                                + "(operators: : > < !). "
                                + "Example: name:john or dob>2003-01-01");
            }

            String key = matcher.group(1);
            String operation = matcher.group(2);
            String value = matcher.group(3);

            if (allowedFields != null && !allowedFields.isEmpty() && !allowedFields.contains(key)) {
                throw new IllegalArgumentException(
                        "Search field '" + key + "' is not allowed. "
                                + "Allowed fields: " + allowedFields);
            }

            criteriaList.add(new SearchCriteria(key, operation, value));
        }

        if (criteriaList.isEmpty()) {
            return null;
        }

        Specification<E> result = createSpecification(criteriaList.get(0));
        for (int i = 1; i < criteriaList.size(); i++) {
            result = result.and(createSpecification(criteriaList.get(i)));
        }

        return result;
    }

    /**
     * Parse search query string using the default allowed fields.
     */
    public static <E> Specification<E> parse(String search) {
        return parse(search, DEFAULT_ALLOWED_SEARCH_FIELDS);
    }

    /**
     * Build a single JPA Specification from SearchCriteria.
     */
    public static <E> Specification<E> createSpecification(SearchCriteria criteria) {
        return (root, query, builder) -> {
            String key = criteria.getKey();
            String operation = criteria.getOperation();
            String valueStr = criteria.getValue() != null ? criteria.getValue().toString() : "";

            // Handle nested property: department.name -> join
            if (key.contains(".")) {
                String[] parts = key.split("\\.", 2);
                Join<?, ?> join = root.join(parts[0]);
                return buildPredicate(join.get(parts[1]), join.get(parts[1]).getJavaType(),
                        operation, valueStr, builder);
            }

            Class<?> fieldType = root.get(key).getJavaType();
            return buildPredicate(root.get(key), fieldType, operation, valueStr, builder);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildPredicate(Path<?> path,
                                           Class<?> fieldType,
                                           String operation,
                                           String valueStr,
                                           CriteriaBuilder builder) {
        Object convertedValue = convertValue(valueStr, fieldType);

        switch (operation) {
            case ":":
                if (fieldType == String.class) {
                    return builder.like(builder.lower(path.as(String.class)),
                            "%" + valueStr.toLowerCase() + "%");
                } else {
                    return builder.equal(path, convertedValue);
                }

            case ">":
                if (Comparable.class.isAssignableFrom(fieldType)) {
                    return builder.greaterThan(path.as((Class<Comparable>) fieldType),
                            (Comparable) convertedValue);
                }
                return builder.greaterThan(path.as(String.class), valueStr);

            case "<":
                if (Comparable.class.isAssignableFrom(fieldType)) {
                    return builder.lessThan(path.as((Class<Comparable>) fieldType),
                            (Comparable) convertedValue);
                }
                return builder.lessThan(path.as(String.class), valueStr);

            case "!":
                return builder.notEqual(path, convertedValue);

            default:
                throw new IllegalArgumentException("Unsupported search operation: " + operation);
        }
    }

    private static Object convertValue(String valueStr, Class<?> fieldType) {
        try {
            if (fieldType == Long.class || fieldType == long.class) {
                return Long.parseLong(valueStr);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(valueStr);
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                return Boolean.parseBoolean(valueStr);
            } else if (fieldType == LocalDate.class) {
                return LocalDate.parse(valueStr);
            } else if (fieldType == Double.class || fieldType == double.class) {
                return Double.parseDouble(valueStr);
            } else if (Date.class.isAssignableFrom(fieldType)) {
                Date parsed = parseDate(valueStr);
                if (fieldType == java.sql.Timestamp.class) {
                    return new java.sql.Timestamp(parsed.getTime());
                } else if (fieldType == java.sql.Date.class) {
                    return new java.sql.Date(parsed.getTime());
                }
                return parsed;
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid value '" + valueStr + "' for field type " + fieldType.getSimpleName()
                            + ". Expected format: " + getExpectedFormat(fieldType));
        }
        return valueStr;
    }

    private static Date parseDate(String valueStr) {
        try {
            if (valueStr.contains(" ")) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(valueStr);
            } else if (valueStr.contains("T")) {
                return Date.from(java.time.Instant.parse(valueStr));
            } else {
                return new SimpleDateFormat("yyyy-MM-dd").parse(valueStr);
            }
        } catch (ParseException | DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date value '" + valueStr + "'. Expected format: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss");
        }
    }

    private static String getExpectedFormat(Class<?> fieldType) {
        if (fieldType == LocalDate.class) return "yyyy-MM-dd";
        if (fieldType == Date.class) return "yyyy-MM-dd or yyyy-MM-dd HH:mm:ss";
        if (fieldType == Long.class || fieldType == long.class) return "numeric (e.g. 123)";
        if (fieldType == Integer.class || fieldType == int.class) return "integer (e.g. 42)";
        if (fieldType == Double.class || fieldType == double.class) return "decimal (e.g. 1.0)";
        if (fieldType == Boolean.class || fieldType == boolean.class) return "true/false";
        return fieldType.getSimpleName();
    }

    public static Set<String> getAllowedSearchFields() {
        return DEFAULT_ALLOWED_SEARCH_FIELDS;
    }

    // =========================================================================
    // 2. PAGINATION LOGIC
    // =========================================================================

    /**
     * Create Pageable with page, size, and optional Sort.
     * Validates bounds according to project requirements:
     * - size between 1 and 100
     * - page >= 0
     *
     * @param page page index (0-based)
     * @param size page size
     * @param sort Sort object (optional)
     * @return Pageable instance
     * @throws IllegalArgumentException if page < 0 or size not between 1 and 100
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new IllegalArgumentException("Page size must be between " + MIN_SIZE + " and " + MAX_SIZE);
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be >= 0");
        }
        if (sort != null) {
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size);
    }

    /**
     * Create Pageable with page and size without explicit sorting.
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, (Sort) null);
    }

    /**
     * Create Pageable by combining pagination and sort parsing parameters in one call.
     */
    public static Pageable createPageable(int page,
                                          int size,
                                          List<String> sortParams,
                                          Set<String> allowedSortFields,
                                          String defaultField,
                                          Sort.Direction defaultDirection) {
        Sort sort = createSort(sortParams, allowedSortFields, defaultField, defaultDirection);
        return createPageable(page, size, sort);
    }

    /**
     * Create Pageable by combining pagination and sort parameters with default fallback.
     */
    public static Pageable createPageable(int page,
                                          int size,
                                          List<String> sortParams,
                                          Set<String> allowedSortFields) {
        return createPageable(page, size, sortParams, allowedSortFields, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
    }

    // =========================================================================
    // 3. SORTING LOGIC
    // =========================================================================

    /**
     * Create Sort from request parameters with field validation and default fallback.
     *
     * @param sortParams       list of sort strings (e.g. ["name,asc", "dob,desc"] or ["name", "asc"])
     * @param allowedFields    set of allowed field names (optional, if null or empty, no field restriction)
     * @param defaultField     default field to sort by if sortParams is null or empty
     * @param defaultDirection default sort direction if sortParams is null or empty
     * @return Sort object
     * @throws IllegalArgumentException if a field is not allowed or direction is invalid
     */
    public static Sort createSort(List<String> sortParams,
                                  Set<String> allowedFields,
                                  String defaultField,
                                  Sort.Direction defaultDirection) {
        Sort.Direction fallbackDirection = defaultDirection != null ? defaultDirection : DEFAULT_SORT_DIRECTION;
        String fallbackField = defaultField != null ? defaultField : DEFAULT_SORT_FIELD;

        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(fallbackDirection, fallbackField);
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sortParams.get(0).contains(",")) {
            for (String sortParam : sortParams) {
                String[] parts = sortParam.split(",");
                String field = parts[0].trim();
                validateSortField(field, allowedFields);

                Sort.Direction direction = Sort.Direction.ASC;
                if (parts.length > 1) {
                    String dir = parts[1].trim().toUpperCase();
                    if ("DESC".equals(dir)) {
                        direction = Sort.Direction.DESC;
                    } else if (!"ASC".equals(dir)) {
                        throw new IllegalArgumentException(
                                "Invalid sort direction: '" + parts[1].trim()
                                        + "'. Must be 'asc' or 'desc'");
                    }
                }
                orders.add(new Sort.Order(direction, field));
            }
        } else {
            int i = 0;
            while (i < sortParams.size()) {
                String field = sortParams.get(i).trim();
                validateSortField(field, allowedFields);

                Sort.Direction direction = Sort.Direction.ASC;
                if (i + 1 < sortParams.size()) {
                    String next = sortParams.get(i + 1).trim();
                    if ("asc".equalsIgnoreCase(next) || "desc".equalsIgnoreCase(next)) {
                        direction = "desc".equalsIgnoreCase(next) ? Sort.Direction.DESC : Sort.Direction.ASC;
                        i++;
                    }
                }
                orders.add(new Sort.Order(direction, field));
                i++;
            }
        }

        return orders.isEmpty() ? Sort.by(fallbackDirection, fallbackField) : Sort.by(orders);
    }

    /**
     * Create Sort with allowed fields validation and default sort by id ASC.
     */
    public static Sort createSort(List<String> sortParams, Set<String> allowedFields) {
        return createSort(sortParams, allowedFields, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
    }

    /**
     * Create Sort without field validation and default sort by id ASC.
     */
    public static Sort createSort(List<String> sortParams) {
        return createSort(sortParams, null, DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
    }

    private static void validateSortField(String field, Set<String> allowedFields) {
        if (allowedFields != null && !allowedFields.isEmpty() && !allowedFields.contains(field)) {
            throw new IllegalArgumentException(
                    "Sort field '" + field + "' is not allowed. "
                            + "Allowed sort fields: " + allowedFields);
        }
    }

    // =========================================================================
    // 4. COMBINED EXECUTION (Search + Pagination + Sorting)
    // =========================================================================

    /**
     * Combined execution helper: parses search, builds pageable with sort, and executes query on repository.
     *
     * @param repository           JpaSpecificationExecutor instance
     * @param search               search filter expression (e.g. "name:Java")
     * @param allowedSearchFields  allowed fields for search
     * @param page                 page index (0-based)
     * @param size                 page size
     * @param sortParams           sort parameter list (e.g. ["name,asc"])
     * @param allowedSortFields    allowed fields for sort
     * @param defaultSortField     default sort field
     * @param defaultSortDirection default sort direction
     * @param <E>                  entity type
     * @return Page of results
     */
    public static <E> Page<E> execute(JpaSpecificationExecutor<E> repository,
                                      String search,
                                      Set<String> allowedSearchFields,
                                      int page,
                                      int size,
                                      List<String> sortParams,
                                      Set<String> allowedSortFields,
                                      String defaultSortField,
                                      Sort.Direction defaultSortDirection) {
        Specification<E> spec = parse(search, allowedSearchFields);
        Pageable pageable = createPageable(page, size, sortParams, allowedSortFields, defaultSortField, defaultSortDirection);
        return repository.findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    /**
     * Combined execution helper using default sort configuration.
     */
    public static <E> Page<E> execute(JpaSpecificationExecutor<E> repository,
                                      String search,
                                      Set<String> allowedSearchFields,
                                      int page,
                                      int size,
                                      List<String> sortParams,
                                      Set<String> allowedSortFields) {
        return execute(repository, search, allowedSearchFields, page, size, sortParams, allowedSortFields,
                DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
    }

    /**
     * Combined execution helper using default allowed search and sort fields.
     */
    public static <E> Page<E> execute(JpaSpecificationExecutor<E> repository,
                                      String search,
                                      int page,
                                      int size,
                                      List<String> sortParams) {
        return execute(repository, search, DEFAULT_ALLOWED_SEARCH_FIELDS, page, size, sortParams, null,
                DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
    }

    // =========================================================================
    // 5. INSTANCE METHODS (For reusable HibernateDao<T>)
    // =========================================================================

    /**
     * Execute search + pagination + sorting on the configured repository.
     */
    public Page<T> find(String search, int page, int size, List<String> sortParams) {
        if (this.repository == null) {
            throw new IllegalStateException("Repository must be provided for instance-based query execution");
        }
        return execute(this.repository, search, this.allowedSearchFields, page, size, sortParams,
                this.allowedSortFields, this.defaultSortField, this.defaultSortDirection);
    }

    /**
     * Execute custom Specification + pagination + sorting on the configured repository.
     */
    public Page<T> find(Specification<T> customSpec, int page, int size, List<String> sortParams) {
        if (this.repository == null) {
            throw new IllegalStateException("Repository must be provided for instance-based query execution");
        }
        Pageable pageable = createPageable(page, size, sortParams, this.allowedSortFields,
                this.defaultSortField, this.defaultSortDirection);
        return this.repository.findAll(customSpec != null ? customSpec : Specification.where(null), pageable);
    }

    // Getters and Setters for instance configuration
    public JpaSpecificationExecutor<T> getRepository() {
        return repository;
    }

    public void setRepository(JpaSpecificationExecutor<T> repository) {
        this.repository = repository;
    }

    public Set<String> getAllowedSearchFieldsConfigured() {
        return allowedSearchFields;
    }

    public void setAllowedSearchFields(Set<String> allowedSearchFields) {
        this.allowedSearchFields = allowedSearchFields;
    }

    public Set<String> getAllowedSortFields() {
        return allowedSortFields;
    }

    public void setAllowedSortFields(Set<String> allowedSortFields) {
        this.allowedSortFields = allowedSortFields;
    }

    public String getDefaultSortField() {
        return defaultSortField;
    }

    public void setDefaultSortField(String defaultSortField) {
        this.defaultSortField = defaultSortField;
    }

    public Sort.Direction getDefaultSortDirection() {
        return defaultSortDirection;
    }

    public void setDefaultSortDirection(Sort.Direction defaultSortDirection) {
        this.defaultSortDirection = defaultSortDirection;
    }
}
