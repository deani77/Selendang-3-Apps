package com.selendang3.app.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotContractTest {
    @Test fun sharedSnapshotContract() {
        val text = javaClass.getResource("/snapshots/cases.json")!!.readText(Charsets.UTF_8)
        val corpus = JSONObject(text)
        val cases = corpus.getJSONArray("cases")
        assertTrue("The shared corpus must not be empty", cases.length() > 0)
        for (index in 0 until cases.length()) {
            val case = cases.getJSONObject(index)
            val snapshot = JSONObject(corpus.getJSONObject("base").toString())
            val overrides = case.getJSONObject("overrides")
            overrides.keys().forEach { key -> snapshot.put(key, overrides.get(key)) }
            val accepted = runCatching { EstatePackages.parse(snapshot.toString()) }.isSuccess
            assertEquals(case.getString("name"), case.getBoolean("valid"), accepted)
        }
    }

    @Test fun totalRowLimit() {
        val corpus = JSONObject(javaClass.getResource("/snapshots/cases.json")!!.readText(Charsets.UTF_8))
        val snapshot = corpus.getJSONObject("base")
        val sections = snapshot.getJSONArray("sections")
        for (i in 0 until sections.length()) sections.getJSONObject(i).put("rows", JSONArray())
        sections.getJSONObject(0).put("rows", JSONArray((1..10000).map { JSONArray(listOf("value", JSONObject.NULL)) }))
        EstatePackages.parse(snapshot.toString())
        sections.getJSONObject(1).put("rows", JSONArray().put(JSONArray(listOf("value", JSONObject.NULL, 0))))
        assertTrue(runCatching { EstatePackages.parse(snapshot.toString()) }.isFailure)
    }

    @Test fun byteAndUtf8Limits() {
        val corpus = JSONObject(javaClass.getResource("/snapshots/cases.json")!!.readText(Charsets.UTF_8))
        val bytes = corpus.getJSONObject("base").toString().toByteArray(Charsets.UTF_8)
        val padded = bytes + ByteArray(EstatePackages.MAX_BYTES - bytes.size) { 32 }
        EstatePackages.parse(EstatePackages.read(padded.inputStream()))
        assertTrue(runCatching { EstatePackages.read(ByteArray(EstatePackages.MAX_BYTES + 1).inputStream()) }.isFailure)
        assertTrue(runCatching { EstatePackages.read((bytes + byteArrayOf(0xff.toByte())).inputStream()) }.isFailure)
    }
}
