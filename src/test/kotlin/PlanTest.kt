import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import ru.barabo.afina.AfinaConnect
import ru.barabo.plan.import.SHEET_NAMES
import ru.barabo.plan.import.loadDBInfo
import ru.barabo.plan.import.processFile
import ru.barabo.plan.import.xlsxFileLoad
import java.io.File

private val logger = LoggerFactory.getLogger(PlanTest::class.java)

class PlanTest {

    @Before
    fun initTestBase() {

        AfinaConnect.init("jdbc:oracle:thin:@192.168.0.43:1521:AFINA", "BARDV", "909957Sn")

        //com.sun.javafx.application.PlatformImpl.startup {}
    }

    //@Test
    fun testLoadDBInfo() {

        val sections = loadDBInfo()

        logger.error("$sections")
    }

    //@Test
    fun testXlsxLoader() {

        val sections = loadDBInfo()

        val file = File("C:/365-П/План_2021_для загрузки.xlsx")

        val info = xlsxFileLoad(file, sections)

        logger.error("$info")
    }

    //@Test
    fun testXlsxSaver() {

        val file = File("C:/365-П/План_2021_для загрузки.xlsx")

        val info = processFile(file)

        logger.error(info)
    }

    //@Test
    fun testFirstKeyHashMap() {

        val mainOffice = SHEET_NAMES.entries.iterator().next().key

        logger.error(mainOffice)
    }
}
