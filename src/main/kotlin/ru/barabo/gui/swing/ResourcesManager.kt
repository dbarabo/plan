package ru.barabo.gui.swing

import java.net.URL
import java.util.*
import javax.swing.ImageIcon


object ResourcesManager {

    private val icoHash: HashMap<String, ImageIcon> = HashMap()

    var icoPath = "/ico/"

    @JvmStatic
    fun getIcon(icoName: String): ImageIcon? =
        icoHash[icoName] ?:  loadIcon(icoName)?.apply { icoHash[icoName] = this }

    private fun loadIcon(icoName: String): ImageIcon? = pathResource("$icoPath$icoName.png")?.let { ImageIcon(it) }

    private fun pathResource(fullPath: String): URL? {

        val path = ResourcesManager::class.java.getResource(fullPath)?.toExternalForm()

        return path?.let{ URL(it) }
    }

    @JvmStatic
    fun getXlsPath(fileXls: String?): URL? {
        val xlsBundle: ResourceBundle = ResourceBundle.getBundle(
            "properties.xls_names", UTF8Control()
        )

        var path = try {
            xlsBundle.getString(fileXls)
        } catch (ex: Exception) {
            logger.error("getXlsPath is null " + ex.localizedMessage)
            null
        }
        logger.error("path=$path")

        if (path == null || path == "") {
            logger.error("path is null")
            return null
        }

        return pathResource("/xls/$path")
    }

    /**
     * computes the path of a resource given its name
     * @param resource the name of a resource
     * @return the full path of a resource
     */
    fun getPath(resource: String?): String? {
        return try {
            ResourcesManager::class.java.getResource(resource)?.toExternalForm()
        } catch (ex: Exception) {
            ""
        }
    }
}