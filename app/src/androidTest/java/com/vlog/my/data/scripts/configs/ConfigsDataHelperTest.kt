package com.vlog.my.data.scripts.configs

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ConfigsDataHelperTest {

    private lateinit var dbHelper: ConfigsDataHelper
    private lateinit var context: Context
    private val defaultDbName = "configs.db" // As defined in ConfigsDataHelper

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(defaultDbName) // Clean slate before each test
        dbHelper = ConfigsDataHelper(context)
    }

    @After
    fun tearDown() {
        dbHelper.close()
        context.deleteDatabase(defaultDbName) // Clean up after each test
    }

    private fun generateUniqueId(): String = UUID.randomUUID().toString()

    // --- Test Cases ---

    @Test
    fun insertAndGetBasicConfig_Minimal() {
        val basicId = generateUniqueId()
        val minimalConfig = BasicsConfig(
            basicId = basicId,
            scriptsId = "test-script-id",
            apiUrlField = "http://example.com/api",
            rootPath = "data"
        )
        dbHelper.insertBasicConfig(minimalConfig)

        val retrievedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull("Retrieved config should not be null", retrievedConfig)
        assertEquals("Basic ID mismatch", basicId, retrievedConfig?.basicId)
        assertEquals("Scripts ID mismatch", "test-script-id", retrievedConfig?.scriptsId)
        assertEquals("API URL mismatch", "http://example.com/api", retrievedConfig?.apiUrlField)
        assertEquals("Root path mismatch", "data", retrievedConfig?.rootPath)
        assertNull("FieldsConfig should be null for minimal config", retrievedConfig?.fieldsConfig)
        assertTrue("BasicMetas list should be empty for minimal config", retrievedConfig?.metaList.isNullOrEmpty())
    }

    @Test
    fun insertAndGetBasicConfig_WithFieldsConfig() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()

        val fieldsConfig = FieldsConfig(
            fieldId = fieldId,
            quoteId = fieldId, // Self-referential for its own metas
            idField = "item.id",
            titleField = "item.title",
            picField = "item.image",
            metaList = mutableListOf(
                MetasConfig(metaId = generateUniqueId(), quoteId = fieldId, metaKey = "field_meta_key1", metaValue = "field_meta_value1")
            )
        )

        val fullConfig = BasicsConfig(
            basicId = basicId,
            scriptsId = "test-script-id",
            apiUrlField = "http://example.com/api/full",
            rootPath = "results",
            fieldsConfig = fieldsConfig
        )
        dbHelper.insertBasicConfig(fullConfig)

        val retrievedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull("Retrieved config should not be null", retrievedConfig)
        assertEquals("Basic ID mismatch", basicId, retrievedConfig?.basicId)
        assertEquals("API URL mismatch", "http://example.com/api/full", retrievedConfig?.apiUrlField)

        assertNotNull("FieldsConfig should not be null", retrievedConfig?.fieldsConfig)
        val retrievedFields = retrievedConfig?.fieldsConfig
        assertEquals("Field ID mismatch", fieldId, retrievedFields?.fieldId)
        assertEquals("ID Field mapping mismatch", "item.id", retrievedFields?.idField)
        assertEquals("Title Field mapping mismatch", "item.title", retrievedFields?.titleField)
        assertEquals("Pic Field mapping mismatch", "item.image", retrievedFields?.picField)
        
        assertNotNull("FieldMetas list should not be null", retrievedFields?.metaList)
        assertEquals("FieldMetas list size mismatch", 1, retrievedFields?.metaList?.size)
        assertEquals("field_meta_key1", retrievedFields?.metaList?.get(0)?.metaKey)
    }

    @Test
    fun insertAndGetBasicConfig_WithBasicMetas() {
        val basicId = generateUniqueId()
        val basicMetas = mutableListOf(
            MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "bm_key1", metaValue = "bm_value1"),
            MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "bm_key2", metaValue = "bm_value2")
        )
        val configWithMetas = BasicsConfig(
            basicId = basicId,
            scriptsId = "test-script-id",
            apiUrlField = "http://example.com/api/metas",
            rootPath = "items",
            metaList = basicMetas
        )
        dbHelper.insertBasicConfig(configWithMetas)

        val retrievedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrievedConfig)
        assertNotNull("BasicMetas list should not be null", retrievedConfig?.metaList)
        assertEquals("BasicMetas list size mismatch", 2, retrievedConfig?.metaList?.size)
        assertTrue("BasicMetas content mismatch", retrievedConfig?.metaList?.any { it.metaKey == "bm_key1" && it.metaValue == "bm_value1" } ?: false)
        assertTrue("BasicMetas content mismatch", retrievedConfig?.metaList?.any { it.metaKey == "bm_key2" && it.metaValue == "bm_value2" } ?: false)
    }
    
    @Test
    fun insertAndGetBasicConfig_Complex() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()

        val fieldMetas = mutableListOf(
            MetasConfig(metaId = generateUniqueId(), quoteId = fieldId, metaKey = "fm_key", metaValue = "fm_value")
        )
        val fieldsConfig = FieldsConfig(
            fieldId = fieldId, quoteId = fieldId, idField = "id", titleField = "title", metaList = fieldMetas
        )
        val basicMetas = mutableListOf(
            MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "bm_key", metaValue = "bm_value")
        )
        val complexConfig = BasicsConfig(
            basicId = basicId, scriptsId = "test-script-id", apiUrlField = "http://complex.api", rootPath = "all",
            metaList = basicMetas, fieldsConfig = fieldsConfig
        )
        dbHelper.insertBasicConfig(complexConfig)

        val retrieved = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrieved)
        assertEquals(basicId, retrieved?.basicId)
        // Basic Metas
        assertEquals(1, retrieved?.metaList?.size)
        assertEquals("bm_key", retrieved?.metaList?.first()?.metaKey)
        // Fields Config
        assertNotNull(retrieved?.fieldsConfig)
        assertEquals(fieldId, retrieved?.fieldsConfig?.fieldId)
        // Field Metas
        assertEquals(1, retrieved?.fieldsConfig?.metaList?.size)
        assertEquals("fm_key", retrieved?.fieldsConfig?.metaList?.first()?.metaKey)
    }


    @Test
    fun getAllBasicConfigs_Empty() {
        val allConfigs = dbHelper.getAllBasicConfigs()
        assertTrue("getAllBasicConfigs should return empty list when DB is empty", allConfigs.isEmpty())
    }

    @Test
    fun getAllBasicConfigs_MultipleItems() {
        val config1 = BasicsConfig(basicId = generateUniqueId(), scriptsId = "ts1", apiUrlField = "url1", rootPath = "rp1")
        val config2 = BasicsConfig(basicId = generateUniqueId(), scriptsId = "ts2", apiUrlField = "url2", rootPath = "rp2",
            fieldsConfig = FieldsConfig(fieldId = generateUniqueId(), quoteId = "self", idField="id", titleField="title")
        )
        dbHelper.insertBasicConfig(config1)
        dbHelper.insertBasicConfig(config2)

        val allConfigs = dbHelper.getAllBasicConfigs()
        assertEquals("getAllBasicConfigs count mismatch", 2, allConfigs.size)
        assertTrue("Config1 not found", allConfigs.any { it.basicId == config1.basicId })
        assertTrue("Config2 not found", allConfigs.any { it.basicId == config2.basicId })
    }

    @Test
    fun updateBasicConfig_DirectProperties() {
        val basicId = generateUniqueId()
        val initialConfig = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "initial_url", rootPath = "initial_path")
        dbHelper.insertBasicConfig(initialConfig)

        val configToUpdate = initialConfig.copy(apiUrlField = "updated_url", rootPath = "updated_path")
        dbHelper.updateBasicConfig(configToUpdate)

        val updatedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(updatedConfig)
        assertEquals("updated_url", updatedConfig?.apiUrlField)
        assertEquals("updated_path", updatedConfig?.rootPath)
        assertEquals("test-script-id", updatedConfig?.scriptsId) // Should not change
    }
    
    @Test
    fun updateBasicConfig_AddFieldsConfig() {
        val basicId = generateUniqueId()
        val initialConfig = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "url", rootPath = "path")
        dbHelper.insertBasicConfig(initialConfig)

        val fieldId = generateUniqueId()
        val fieldsConfigToAdd = FieldsConfig(fieldId = fieldId, quoteId = fieldId, idField = "new_id", titleField = "new_title")
        val configToUpdate = initialConfig.copy(fieldsConfig = fieldsConfigToAdd)
        dbHelper.updateBasicConfig(configToUpdate)

        val updatedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(updatedConfig?.fieldsConfig)
        assertEquals(fieldId, updatedConfig?.fieldsConfig?.fieldId)
        assertEquals("new_id", updatedConfig?.fieldsConfig?.idField)
    }

    @Test
    fun updateBasicConfig_ModifyExistingFieldsConfig() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()
        val initialFieldsConfig = FieldsConfig(fieldId = fieldId, quoteId = fieldId, idField = "old_id", titleField = "old_title")
        val initialConfig = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "url", rootPath = "path", fieldsConfig = initialFieldsConfig)
        dbHelper.insertBasicConfig(initialConfig)

        val modifiedFieldsConfig = initialFieldsConfig.copy(titleField = "updated_title", picField = "new_pic")
        val configToUpdate = initialConfig.copy(fieldsConfig = modifiedFieldsConfig)
        dbHelper.updateBasicConfig(configToUpdate)
        
        val updatedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(updatedConfig?.fieldsConfig)
        assertEquals(fieldId, updatedConfig?.fieldsConfig?.fieldId) // ID should remain same
        assertEquals("old_id", updatedConfig?.fieldsConfig?.idField)
        assertEquals("updated_title", updatedConfig?.fieldsConfig?.titleField)
        assertEquals("new_pic", updatedConfig?.fieldsConfig?.picField)
    }

    @Test
    fun updateBasicConfig_ChangeBasicMetas() {
        val basicId = generateUniqueId()
        val initialMetas = mutableListOf(MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "key1", metaValue = "val1"))
        val initialConfig = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "url", rootPath = "path", metaList = initialMetas)
        dbHelper.insertBasicConfig(initialConfig)

        val updatedMetas = mutableListOf(
            MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "key2", metaValue = "val2"), // New meta
            MetasConfig(metaId = initialMetas[0].metaId, quoteId = basicId, metaKey = "key1_updated", metaValue = "val1_updated") // Modified meta (assuming update logic re-inserts)
        )
        // The current updateBasicConfig re-inserts metas based on the new list.
        // So, if a meta from initialMetas is not in updatedMetas, it's removed.
        // If a meta in updatedMetas has an ID that was in initialMetas, it might be updated or re-inserted.
        // Given the `deleteMetasConfigByQuoteId` and re-insert pattern, new IDs might be generated for all metas on update.
        // Let's adjust the expectation: we give a new list, and that new list should be there.
        
        val newMetasForUpdate = mutableListOf(
             MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "new_key_A", metaValue = "new_val_A"),
             MetasConfig(metaId = generateUniqueId(), quoteId = basicId, metaKey = "new_key_B", metaValue = "new_val_B")
        )

        val configToUpdate = initialConfig.copy(metaList = newMetasForUpdate)
        dbHelper.updateBasicConfig(configToUpdate)

        val updatedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(updatedConfig?.metaList)
        assertEquals(2, updatedConfig?.metaList?.size)
        assertTrue(updatedConfig?.metaList?.any { it.metaKey == "new_key_A" } ?: false)
        assertTrue(updatedConfig?.metaList?.any { it.metaKey == "new_key_B" } ?: false)
    }
    
    @Test
    fun updateBasicConfig_ChangeFieldMetas() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()
        val initialFieldMetas = mutableListOf(MetasConfig(metaId = generateUniqueId(), quoteId = fieldId, metaKey = "fk1", metaValue = "fv1"))
        val initialFieldsConfig = FieldsConfig(fieldId = fieldId, quoteId = fieldId, idField = "id", titleField = "title", metaList = initialFieldMetas)
        val initialConfig = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "url", rootPath = "path", fieldsConfig = initialFieldsConfig)
        dbHelper.insertBasicConfig(initialConfig)

        val updatedFieldMetas = mutableListOf(MetasConfig(metaId = generateUniqueId(), quoteId = fieldId, metaKey = "fk_new", metaValue = "fv_new"))
        val configToUpdate = initialConfig.copy(
            fieldsConfig = initialFieldsConfig.copy(metaList = updatedFieldMetas)
        )
        dbHelper.updateBasicConfig(configToUpdate)

        val updatedConfig = dbHelper.getBasicConfigById(basicId)
        assertNotNull(updatedConfig?.fieldsConfig?.metaList)
        assertEquals(1, updatedConfig?.fieldsConfig?.metaList?.size)
        assertEquals("fk_new", updatedConfig?.fieldsConfig?.metaList?.first()?.metaKey)
    }


    @Test
    fun deleteBasicConfig_Cascades() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()
        val basicMetaId1 = generateUniqueId()
        val fieldMetaId1 = generateUniqueId()

        val fieldMetas = mutableListOf(MetasConfig(metaId = fieldMetaId1, quoteId = fieldId, metaKey = "fm1", metaValue = "fv1"))
        val fieldsConfig = FieldsConfig(fieldId = fieldId, quoteId = fieldId, idField = "id", titleField = "title", metaList = fieldMetas)
        val basicMetas = mutableListOf(MetasConfig(metaId = basicMetaId1, quoteId = basicId, metaKey = "bm1", metaValue = "bv1"))
        
        val config = BasicsConfig(basicId = basicId, scriptsId = "test-script-id", apiUrlField = "url", rootPath = "path", metaList = basicMetas, fieldsConfig = fieldsConfig)
        dbHelper.insertBasicConfig(config)

        // Ensure everything is inserted
        assertNotNull(dbHelper.getBasicConfigById(basicId))
        assertNotNull(dbHelper.getFieldsConfigById(fieldId)) // Assumes FieldsConfig is retrievable like this
        assertEquals(1, dbHelper.getMetasConfigByQuoteId(basicId).size)
        assertEquals(1, dbHelper.getMetasConfigByQuoteId(fieldId).size)

        dbHelper.deleteBasicConfig(basicId)

        assertNull("Deleted BasicConfig should be null", dbHelper.getBasicConfigById(basicId))
        // Check cascade: FieldsConfig and Metas should also be gone.
        // The current ConfigsDataHelper.deleteBasicConfig explicitly deletes associated FieldsConfig,
        // and FieldsConfig deletion deletes its Metas. BasicConfig deletion also deletes its own Metas.
        assertTrue("Metas for BasicConfig should be deleted", dbHelper.getMetasConfigByQuoteId(basicId).isEmpty())
        
        // To fully test cascade on FieldsConfig, we need to ensure it's not just the FK link being set to NULL.
        // Since deleteBasicConfig calls deleteFieldsConfig, the FieldsConfig row itself should be gone.
        // A direct getFieldsConfigById might not be the best test if it relies on a BasicsConfig existing.
        // However, if getFieldsConfigById can fetch independently:
        val retrievedFieldsAfterDelete = dbHelper.getFieldsConfigById(fieldId)
        assertNull("Associated FieldsConfig should be deleted", retrievedFieldsAfterDelete)

        assertTrue("Metas for FieldsConfig should be deleted", dbHelper.getMetasConfigByQuoteId(fieldId).isEmpty())
    }
    
    @Test
    fun deleteBasicConfig_DoesNotAffectOthers() {
        val basicId1 = generateUniqueId()
        val basicId2 = generateUniqueId()
        val config1 = BasicsConfig(basicId = basicId1, scriptsId = "ts1", apiUrlField = "url1", rootPath = "rp1")
        val config2 = BasicsConfig(basicId = basicId2, scriptsId = "ts2", apiUrlField = "url2", rootPath = "rp2")
        dbHelper.insertBasicConfig(config1)
        dbHelper.insertBasicConfig(config2)

        dbHelper.deleteBasicConfig(basicId1)

        assertNull(dbHelper.getBasicConfigById(basicId1))
        assertNotNull(dbHelper.getBasicConfigById(basicId2))
        assertEquals(1, dbHelper.getAllBasicConfigs().size)
    }

    @Test
    fun getNonExistentConfig() {
        assertNull(dbHelper.getBasicConfigById("non_existent_id"))
    }

    @Test
    fun updateNonExistentConfig() {
        val nonExistentConfig = BasicsConfig(basicId = "non_existent", scriptsId = "ts", apiUrlField = "url", rootPath = "rp")
        val updateResult = dbHelper.updateBasicConfig(nonExistentConfig) // updateBasicConfig returns Int (rows affected)
        assertEquals("Updating non-existent config should affect 0 rows", 0, updateResult)
    }

    @Test
    fun deleteNonExistentConfig() {
        val deleteResult = dbHelper.deleteBasicConfig("non_existent") // deleteBasicConfig returns Int (rows affected)
        assertEquals("Deleting non-existent config should affect 0 rows", 0, deleteResult)
    }
    
    @Test
    fun insertBasicConfig_WithNullOptionalFields() {
        val basicId = generateUniqueId()
        val config = BasicsConfig(
            basicId = basicId,
            scriptsId = "test-script-id",
            apiUrlField = "http://example.com/api",
            urlParamsField = null, // Optional
            rootPath = "data",
            metaList = null, // Optional
            fieldsConfig = null // Optional
        )
        dbHelper.insertBasicConfig(config)
        val retrieved = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrieved)
        assertEquals(basicId, retrieved?.basicId)
        assertNull(retrieved?.urlParamsField)
        assertTrue(retrieved?.metaList.isNullOrEmpty()) // Stored as empty list if null
        assertNull(retrieved?.fieldsConfig)
    }

    @Test
    fun insertBasicConfig_WithEmptyOptionalFields() {
        val basicId = generateUniqueId()
        val config = BasicsConfig(
            basicId = basicId,
            scriptsId = "test-script-id",
            apiUrlField = "http://example.com/api",
            urlParamsField = "", // Optional
            rootPath = "data",
            metaList = mutableListOf(), // Optional
            fieldsConfig = FieldsConfig( // FieldsConfig with empty optionals
                fieldId = generateUniqueId(),
                quoteId = "self", 
                idField = "id", titleField = "title",
                picField = "", contentField = null, tagsField = "", sourceUrlField = null,
                metaList = mutableListOf()
            )
        )
        dbHelper.insertBasicConfig(config)
        val retrieved = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrieved)
        assertEquals(basicId, retrieved?.basicId)
        // Empty strings for nullable fields are stored as is by ContentValue.put(key, String)
        // The retrieval logic in ConfigsDataHelper will read them back as empty strings.
        assertEquals("", retrieved?.urlParamsField) 
        assertTrue(retrieved?.metaList.isNullOrEmpty())

        assertNotNull(retrieved?.fieldsConfig)
        assertEquals("", retrieved?.fieldsConfig?.picField)
        assertNull(retrieved?.fieldsConfig?.contentField) // Nulls should remain null
        assertTrue(retrieved?.fieldsConfig?.metaList.isNullOrEmpty())
    }
    
    @Test
    fun foreignKey_BasicsToFields_OnDeleteSetNull() {
        val basicId = generateUniqueId()
        val fieldId = generateUniqueId()
        val fieldsConfig = FieldsConfig(fieldId = fieldId, quoteId = fieldId, idField = "id", titleField = "title")
        val basicConfig = BasicsConfig(basicId = basicId, scriptsId = "test", apiUrlField = "url", rootPath = "root", fieldsConfig = fieldsConfig)

        dbHelper.insertBasicConfig(basicConfig)
        var retrievedBasic = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrievedBasic)
        assertNotNull(retrievedBasic?.fieldsConfig)
        assertEquals(fieldId, retrievedBasic?.fieldsConfig?.fieldId)

        // Delete FieldsConfig directly (this tests the FK constraint from BasicsConfig table)
        dbHelper.deleteFieldsConfig(fieldId) // This also deletes metas of FieldsConfig

        // Fetch BasicsConfig again, its fieldId_fk should be null
        retrievedBasic = dbHelper.getBasicConfigById(basicId)
        assertNotNull(retrievedBasic) // BasicConfig itself should still exist
        assertNull("fieldId_fk in BasicsConfig should be null after FieldsConfig is deleted", retrievedBasic?.fieldsConfig)

        // Also ensure FieldsConfig is actually gone
        assertNull("FieldsConfig should be deleted", dbHelper.getFieldsConfigById(fieldId))
    }
}
