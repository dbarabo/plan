package ru.barabo.gui.swing

import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.*


class UTF8Control : ResourceBundle.Control() {

    @Throws(IllegalAccessException::class, InstantiationException::class, IOException::class)
    override fun newBundle(
        baseName: String?,
        locale: Locale?,
        format: String?,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle? {
        // The below is a copy of the default implementation.
        val bundleName: String = toBundleName(baseName, locale)
        val resourceName: String = toResourceName(bundleName, "properties")
        var bundle: ResourceBundle? = null
        var stream: InputStream? = null
        if (reload) {
            val url: URL? = loader.getResource(resourceName)
            if (url != null) {
                val connection: URLConnection = url.openConnection()

                connection.useCaches = false
                stream = connection.getInputStream()
            }
        } else {
            stream = loader.getResourceAsStream(resourceName)
        }
        if (stream != null) {
            bundle = try {
                // Only this line is changed to make it to read properties files as UTF-8.
                PropertyResourceBundle(InputStreamReader(stream, "UTF-8"))
            } finally {
                stream.close()
            }
        }
        return bundle
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UTF8Control::class.java)

        @JvmStatic
        fun getXlsPath(fileXls: String?): URL? {

            fileXls ?: return null

            val xlsBundle: ResourceBundle = ResourceBundle.getBundle(
                "properties.xls_names", UTF8Control()
            )

            val path = try {
                xlsBundle.getString(fileXls)
            } catch (ex: Exception) {
                logger.error("getXlsPath is null " + ex.localizedMessage)
                null
            }

            if (path == null || path == "") {
                return null
            }

            return ResourcesManager.pathResource("/xls/$path")
        }
    }
}



