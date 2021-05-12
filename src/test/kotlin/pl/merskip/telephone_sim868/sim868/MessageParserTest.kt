package pl.merskip.telephone_sim868.sim868

import org.junit.Assert
import org.junit.Test

internal class MessageParserTest {

    @Test
    fun parseJustOk() {
        val response = ResponseParser().parse("OK\r\n")
        Assert.assertEquals(Message.Status.OK, response.status)
        Assert.assertTrue(response.entities.isEmpty())
    }

    @Test
    fun parseJustError() {
        val response = ResponseParser().parse("ERROR\r\n")
        Assert.assertEquals(Message.Status.ERROR, response.status)
        Assert.assertTrue(response.entities.isEmpty())
    }

    @Test
    fun parseSingleEntity() {
        val response = ResponseParser().parse(
            "+CPIN: READY\r\n" +
                    "\r\n" +
                    "OK\r\n"
        )
        Assert.assertEquals(Message.Status.OK, response.status)
        Assert.assertEquals("READY", response["+CPIN"][0].string)
    }

    @Test
    fun parseMultipleEntity() {
        val response = ResponseParser().parse(
            "+CPOL: 1,2,\"20801\"\r\n" +
                    "+CPOL: 2,2,\"23101\"\r\n" +
                    "+CPOL: 3,2,\"23433\"\r\n" +
                    "\r\n" +
                    "OK\r\n"
        )
        Assert.assertEquals(Message.Status.OK, response.status)
        Assert.assertEquals(3, response.getList("+CPOL").size)

        Assert.assertEquals(1, response["+CPOL", 0][0].integer)
        Assert.assertEquals(2, response["+CPOL", 0][1].integer)
        Assert.assertEquals("20801", response["+CPOL", 0][2].string)

        Assert.assertEquals(2, response["+CPOL", 1][0].integer)
        Assert.assertEquals(2, response["+CPOL", 1][1].integer)
        Assert.assertEquals("23101", response["+CPOL", 1][2].string)

        Assert.assertEquals(3, response["+CPOL", 2][0].integer)
        Assert.assertEquals(2, response["+CPOL", 2][1].integer)
        Assert.assertEquals("23433", response["+CPOL", 2][2].string)
    }

    @Test
    fun parseWithDataAndOk() {
        val response = ResponseParser().parse(
            "+CMGR: 0,\"\",23\r\n" +
                    "07918406010023F0040B918427768514F700001250019163708004D4F29C0E\r\n" +
                    "\r\n" +
                    "OK\r\n"
        )

        Assert.assertEquals(Message.Status.OK, response.status)
        Assert.assertEquals(0, response["+CMGR"][0].integer)
        Assert.assertEquals("", response["+CMGR"][1].string)
        Assert.assertEquals(23, response["+CMGR"][2].integer)
        Assert.assertEquals("07918406010023F0040B918427768514F700001250019163708004D4F29C0E", response["+CMGR"].data)
    }

    @Test
    fun parseDataWithoutCommand() {
        val response = ResponseParser().parse(
            "917533699292936\r\n" +
                    "\r\n" +
                    "OK\r\n"
        )

        Assert.assertEquals(Message.Status.OK, response.status)
        Assert.assertEquals("917533699292936", response.data)
    }
}