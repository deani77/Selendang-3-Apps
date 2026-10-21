package com.selendang3.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ProductionDraftStore(context: Context) {
    private val preferences = context.getSharedPreferences("production_drafts_v1", Context.MODE_PRIVATE)
    fun load(): List<ProductionDraft> {
        val root = JSONObject(preferences.getString("records", null) ?: "{\"version\":1,\"rows\":[]}")
        require(root.getInt("version") == 1) { "Unsupported production draft format." }
        val rows = root.getJSONArray("rows")
        val result = (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            fun number(key: String): Double? = if (row.isNull(key)) null else row.getDouble(key)
            val workers = number("harvesters")
            require(workers == null || (workers.isFinite() && workers >= 0 && workers <= Int.MAX_VALUE && workers % 1.0 == 0.0))
            ProductionDraft(row.getString("date"), row.getString("field"), number("harvested"),
                number("sentToMill"), number("backlog"), number("hectares"), workers?.toInt()).also { it.validate() }
        }
        require(result.map { it.key }.distinct().size == result.size) { "Duplicate production drafts." }
        return result
    }

    fun save(draft: ProductionDraft): List<ProductionDraft> {
        draft.validate()
        // Read failures propagate: never replace unreadable saved data with an empty set.
        val updated = (load().filterNot { it.key == draft.key } + draft).sortedBy { it.key }
        val rows = JSONArray()
        updated.forEach { row ->
            rows.put(JSONObject().put("date", row.date).put("field", row.field)
                .put("harvested", row.harvested ?: JSONObject.NULL)
                .put("sentToMill", row.sentToMill ?: JSONObject.NULL)
                .put("backlog", row.backlog ?: JSONObject.NULL)
                .put("hectares", row.hectares ?: JSONObject.NULL)
                .put("harvesters", row.harvesters ?: JSONObject.NULL))
        }
        check(preferences.edit().putString("records", JSONObject().put("version", 1).put("rows", rows).toString()).commit()) {
            "Could not save production draft. Try again."
        }
        return updated
    }
}
