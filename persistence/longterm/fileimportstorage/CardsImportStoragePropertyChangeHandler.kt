package com.chakulafasta.pom.persistence.longterm.fileimportstorage

import com.chakulafasta.pom.Database
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.chakulafasta.pom.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsFileFormat
import com.chakulafasta.pom.domain.interactor.cardsimport.CardsImportStorage
import com.chakulafasta.pom.persistence.DbKeys
import com.chakulafasta.pom.persistence.FileFormatDb
import com.chakulafasta.pom.persistence.longterm.PropertyChangeHandler
import com.chakulafasta.pom.persistence.toFileFormatDb

class CardsImportStoragePropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    override fun handle(change: Change) {
        when (change.property) {
            CardsImportStorage::customFileFormats -> {
                if (change !is CollectionChange) return

                val removedFileFormats = change.removedItems as Collection<CardsFileFormat>
                removedFileFormats.forEach { fileFormat: CardsFileFormat ->
                    database.fileFormatQueries.delete(fileFormat.id)
                }

                val addedFileFormats = change.addedItems as Collection<CardsFileFormat>
                addedFileFormats.forEach { fileFormat: CardsFileFormat ->
                    val fileFormatDb: FileFormatDb = fileFormat.toFileFormatDb()
                    database.fileFormatQueries.insert(fileFormatDb)
                }
            }
            CardsImportStorage::lastUsedEncodingName -> {
                if (change !is PropertyValueChange) return
                val lastUsedEncodingName = change.newValue as String
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_ENCODING_NAME,
                    value = lastUsedEncodingName
                )
            }
            CardsImportStorage::lastUsedFormatForTxt -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForTxt = change.newValue as CardsFileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TXT,
                    value = lastUsedFormatForTxt.id.toString()
                )
            }
            CardsImportStorage::lastUsedFormatForCsv -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForCsv = change.newValue as CardsFileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_CSV,
                    value = lastUsedFormatForCsv.id.toString()
                )
            }
            CardsImportStorage::lastUsedFormatForTsv -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForTsv = change.newValue as CardsFileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TSV,
                    value = lastUsedFormatForTsv.id.toString()
                )
            }
        }
    }
}