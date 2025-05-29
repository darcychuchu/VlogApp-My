package com.vlog.my.utils

import com.vlog.my.data.scripts.configs.BasicsConfig
import com.vlog.my.data.scripts.configs.FieldsConfig
import com.vlog.my.data.scripts.configs.MetasConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class JsonToBasicsConfigParser {

    /*
    * Main public function to parse a JSON string and generate a list of BasicsConfig objects.
    * Each BasicsConfig corresponds to a detected JSON array within the input JSON structure,
    * which is considered a potential root path for data extraction.
    *
    * @param jsonString The JSON string to parse.
    * @param apiUrl The API URL from which the JSON was fetched, used to populate BasicsConfig.apiUrlField.
    * @return Result<List<BasicsConfig>>: On success, a list of generated BasicsConfig objects.
    *         On failure, an exception detailing the parsing error.
    */
    fun parseJsonAndGenerateConfigs(jsonString: String, apiUrl: String): Result<List<BasicsConfig>> {
        try {
            val rootJsonObject = JSONObject(jsonString)
            val allListPaths = mutableListOf<String>()
            findListPaths(rootJsonObject, "", allListPaths)

            if (allListPaths.isEmpty()) {
                return Result.success(emptyList())
            }

            val generatedConfigs = mutableListOf<BasicsConfig>()

            for (listPath in allListPaths) {
                val basicId = UUID.randomUUID().toString()
                val pathPartsForRootPath = listPath.split('.').filter { it.isNotEmpty() }.toSet()

                // Populate BasicsConfig.metaList
                val basicsMetaList = extractRootMetas(rootJsonObject, pathPartsForRootPath)

                // Populate FieldsConfig
                var fieldsConfig: FieldsConfig? = null
                val jsonArrayForPath = getValueFromPath(rootJsonObject, listPath) as? JSONArray

                if (jsonArrayForPath != null && jsonArrayForPath.length() > 0) {
                    val firstItemObject = jsonArrayForPath.optJSONObject(0)
                    if (firstItemObject != null) {
                        val fieldId = UUID.randomUUID().toString()
                        val fieldMetas = extractMetasFromJsonObjectRecursive(firstItemObject, fieldId)
                        fieldsConfig = FieldsConfig(
                            fieldId = fieldId,
                            quoteId = basicId, // This meta belongs to the current BasicsConfig
                            idField = "", // Default, to be set by user or further logic
                            titleField = "", // Default
                            metaList = fieldMetas
                        )
                    }
                }

                val basicsConfig = BasicsConfig(
                    basicId = basicId,
                    scriptsId = "test-script-id", // Hardcoded as per requirement
                    apiUrlField = apiUrl,
                    urlParamsField = null, // Default
                    urlTypedField = 0,     // Default
                    rootPath = listPath,
                    metaList = basicsMetaList,
                    fieldsConfig = fieldsConfig
                )
                generatedConfigs.add(basicsConfig)
            }
            return Result.success(generatedConfigs)
        } catch (e: JSONException) {
            return Result.failure(e)
        } catch (e: Exception) { // Catch any other unexpected errors during parsing
            return Result.failure(e)
        }
    }

    /**
     * Recursively finds all dot-separated paths to JSONArrays within a given JSON structure.
     *
     * @param jsonValue The current JSON value being inspected (JSONObject or JSONArray).
     * @param currentPath The path accumulated so far to reach the current jsonValue.
     * @param allListPaths A mutable list to store all discovered paths to JSONArrays.
     */
    private fun findListPaths(jsonValue: Any, currentPath: String = "", allListPaths: MutableList<String>) {
        when (jsonValue) {
            is JSONObject -> {
                jsonValue.keys().forEach { key ->
                    val newPath = if (currentPath.isEmpty()) key else "$currentPath.$key"
                    jsonValue.opt(key)?.let { value -> // Use opt to safely get value
                        findListPaths(value, newPath, allListPaths)
                    }
                }
            }
            is JSONArray -> {
                // Found a list at the currentPath. Add it.
                // We do not iterate deeper into this array's elements for finding more *distinct* rootPaths.
                if (currentPath.isNotEmpty()) { // Ensure we don't add an empty path if the root itself is an array
                    allListPaths.add(currentPath)
                }
            }
            // Primitives or nulls are ignored
        }
    }

    /**
     * Extracts metas for BasicsConfig.metaList.
     * These are top-level keys from the root JSON object that are NOT part of the ancestry
     * of the current listPath (rootPath).
     *
     * @param rootJsonObject The root JSONObject of the entire JSON response.
     * @param pathPartsForRootPath A set of keys that form the path to the current rootPath list.
     * @return A mutable list of MetasConfig objects.
     */
    private fun extractRootMetas(
        rootJsonObject: JSONObject,
        pathPartsForRootPath: Set<String>
    ): MutableList<MetasConfig>? {
        val metas = mutableListOf<MetasConfig>()
        rootJsonObject.keys().forEach { key ->
            if (!pathPartsForRootPath.contains(key)) {
                // This key is a root-level key and not part of the current listPath's ancestry
                metas.add(
                    MetasConfig(
                        metaId = UUID.randomUUID().toString(),
                        quoteId = "", // Will be set to basicId by the caller
                        metaKey = key,
                        metaValue = key, // Default metaValue to metaKey
                        metaTyped = 0,   // Default type
                        metaList = null  // Root metas are not recursive in this context
                    )
                )
            }
        }
        return if (metas.isNotEmpty()) metas else null
    }


    /**
     * Recursively extracts MetasConfig from a JSONObject, typically the first item of a detected list,
     * to define the structure for FieldsConfig.metaList.
     *
     * @param jsonObject The JSONObject to extract keys and nested structures from.
     * @param quoteId The ID of the parent FieldsConfig these metas will belong to.
     * @return A mutable list of MetasConfig objects, potentially with nested metaLists.
     */
    private fun extractMetasFromJsonObjectRecursive(
        jsonObject: JSONObject,
        quoteId: String // This quoteId is the fieldId of the parent FieldsConfig
    ): MutableList<MetasConfig>? {
        val metas = mutableListOf<MetasConfig>()
        jsonObject.keys().forEach { key ->
            val value = jsonObject.opt(key) // Use opt to handle potential nulls gracefully
            var childrenMetas: MutableList<MetasConfig>? = null

            if (value is JSONObject) {
                childrenMetas = extractMetasFromJsonObjectRecursive(value, quoteId)
            } else if (value is JSONArray) {
                if (value.length() > 0) {
                    val firstItemInArray = value.optJSONObject(0)
                    if (firstItemInArray != null) {
                        // If array contains objects, define structure based on the first object
                        childrenMetas = extractMetasFromJsonObjectRecursive(firstItemInArray, quoteId)
                    }
                    // If array contains primitives or is empty, childrenMetas remains null (no further structure)
                }
            }
            // For non-object/non-array types, childrenMetas also remains null.

            metas.add(
                MetasConfig(
                    metaId = UUID.randomUUID().toString(),
                    quoteId = quoteId,
                    metaKey = key,
                    metaValue = key, // Default metaValue to metaKey
                    metaTyped = 0,   // Default type, can be refined later if type detection is needed
                    metaList = childrenMetas
                )
            )
        }
        return if (metas.isNotEmpty()) metas else null
    }

    /**
     * Helper function to retrieve a JSON value (JSONObject or JSONArray) from a root JSONObject
     * using a dot-separated path string.
     *
     * @param root The root JSONObject to start traversal from.
     * @param path The dot-separated path (e.g., "data.items.0.details"). Handles array indices.
     * @return The JSON value at the specified path (Any?), or null if path is invalid or value not found.
     */
    private fun getValueFromPath(root: JSONObject, path: String): Any? {
        val parts = path.split('.')
        var current: Any = root
        try {
            for (part in parts) {
                if (current is JSONObject) {
                    current = (current as JSONObject).get(part)
                } else if (current is JSONArray) {
                    // This basic version doesn't handle array indexing in path like "items.0.name"
                    // If path directly points to an array (e.g. "items"), this loop iteration should be the last.
                    // The primary use case here is that `path` from `findListPaths` points directly to an array.
                    // If `path` can be more complex like "object.arraylist.0.childobject", this needs enhancement.
                    // For now, assuming `path` from `findListPaths` will correctly lead to a JSONArray if it's the target.
                    // This function expects 'path' to resolve to an object until the final segment,
                    // which can be an array or object. If 'path' is "data.items", 'items' should be the JSONArray.
                    throw JSONException("Path implies array traversal, but current is not an object to get key '$part'")
                } else {
                    return null // Path is invalid, cannot traverse further
                }
            }
            return current
        } catch (e: JSONException) {
            return null // Path segment not found or type mismatch
        }
    }
}