package com.kelin.skuselector

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kelin.skuselector.model.JsonData
import com.kelin.skuselector.model.SKUInfo
import com.kelin.skuselector.model.SpecInfo
import com.kelin.skuselector.model.SpecGroup
import com.kelin.skuselector.sku.SKUSelector
import com.kelin.skuselector.sku.Spec
import com.kelin.skuselector.widget.FlowLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val specGroupLayouts by lazy { ArrayList<FlowLayout>() }

    private lateinit var skuSelector: SKUSelector
    private val skuList by lazy {
        Gson().fromJson<List<SKUInfo>>(
            JsonData.value,
            object : TypeToken<List<SKUInfo>>() {}.type
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val specs = skuList.flatMap { it.specList }.distinctBy { it.specId }
        skuSelector = SKUSelector(skuList, specs)
        val specGroups = getSpecGroups(specs)
        llSpecsContainer.apply {
            specGroups.forEach { d ->
                addView(
                    LayoutInflater.from(context).inflate(R.layout.layout_speces_group, this, false)
                        .also { iv ->
                            iv.findViewById<TextView>(R.id.tvSpecGroupName).text = d.name
                            iv.findViewById<FlowLayout>(R.id.flSpecs).apply {
                                specGroupLayouts.add(this)
                                setOnTagClickListener<SpecInfo>(true) { _, tagView, tag, _ ->
                                    if (tagView.isSelected) {
                                        skuSelector.removeSelected(tag)
                                    } else {
                                        skuSelector.addSelected(tag)
                                    }
                                    specGroupLayouts.forEach { it.refresh() }
                                    refreshSelectedSku(skuSelector.selected)
                                }
                                setTags(d.specs)
                            }
                        }
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSelectedSku(selected: List<Spec>) {
        skuList.find { it.specList.size == selected.size && it.hasSpecs(selected) }.also { sku ->
            tvCommodityName.text = sku?.specsName
            tvPrice.text = if (sku == null) null else "￥${sku.price / 100}"
        }
    }

    private fun getSpecGroups(specs: List<SpecInfo>): List<SpecGroup> {
        return specs.groupBy { it.groupName }
            .mapNotNull {
                if (it.value.isNullOrEmpty()) {
                    null
                } else {
                    SpecGroup(it.key, it.value)
                }
            }
    }
}