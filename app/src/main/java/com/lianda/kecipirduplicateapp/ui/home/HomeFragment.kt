package com.lianda.kecipirduplicateapp.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.kennyc.view.MultiStateView

import com.lianda.kecipirduplicateapp.R
import com.lianda.kecipirduplicateapp.data.model.Category
import com.lianda.kecipirduplicateapp.data.model.Product
import com.lianda.kecipirduplicateapp.depth.service.model.Resource
import com.lianda.kecipirduplicateapp.ui.groupie.BannerGroupieItem
import com.lianda.kecipirduplicateapp.ui.groupie.CategoryGroupieItem
import com.lianda.kecipirduplicateapp.ui.groupie.HeaderGroupieItem
import com.lianda.kecipirduplicateapp.ui.groupie.ProductGroupieItem
import com.lianda.kecipirduplicateapp.ui.product.ProductDetailActivity
import com.lianda.kecipirduplicateapp.ui.viewmodel.ProductViewModel
import com.lianda.kecipirduplicateapp.utils.getCurrentDate
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val groupieAdapter = GroupAdapter<GroupieViewHolder>()

    private val productViewModel:ProductViewModel by viewModel()

    private var bannerGroupieItem:BannerGroupieItem ? = null
    private var categoryGroupieItem:CategoryGroupieItem ? = null
    private var popularProductGroupieItem:ProductGroupieItem ? = null
    private var allProductGroupieItem:ProductGroupieItem ? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCurrentDate()
        initGroupie()

        productViewModel.getProducts()
        observeData()
    }

    private fun initGroupie(){
        bannerGroupieItem = BannerGroupieItem(requireContext(), mutableListOf()) { product->
            toProductDetail(product)
        }
        categoryGroupieItem = CategoryGroupieItem(requireContext(), mutableListOf())
        popularProductGroupieItem = ProductGroupieItem(requireContext(), mutableListOf()){ product->
            toProductDetail(product)
        }
        allProductGroupieItem = ProductGroupieItem(requireContext(), mutableListOf()){ product->
            toProductDetail(product)
        }
    }

    private fun showCurrentDate(){
        tvDeliveryDate.text = getCurrentDate()
    }

    private fun observeData(){
        productViewModel.products.observe(this, Observer {
            when(it){
                is Resource.Loading -> showHomeLoading()
                is Resource.Empty -> {Log.d("product", "empty")}
                is Resource.Success -> showHomeContent(it.data)
                is Resource.Error -> {Log.d("product", "error ${it.message}")}
            }
        })
    }

    private fun showHomeLoading(){
        bannerGroupieItem?.viewState = MultiStateView.ViewState.LOADING
        categoryGroupieItem?.viewState = MultiStateView.ViewState.LOADING
        popularProductGroupieItem?.viewState = MultiStateView.ViewState.LOADING
        allProductGroupieItem?.viewState = MultiStateView.ViewState.LOADING
    }

    private fun showHomeContent(datas: List<Product>){
        bannerGroupieItem?.viewState = MultiStateView.ViewState.CONTENT
        categoryGroupieItem?.viewState = MultiStateView.ViewState.CONTENT
        popularProductGroupieItem?.viewState = MultiStateView.ViewState.CONTENT
        allProductGroupieItem?.viewState = MultiStateView.ViewState.CONTENT

        groupieAdapter.add(HeaderGroupieItem("Produk Diskon Hari Ini"))
        groupieAdapter.add(showBannerItem(datas))

        groupieAdapter.add(HeaderGroupieItem("Kategori Produk"))
        groupieAdapter.add(showCategoryItem(datas))

        groupieAdapter.add(HeaderGroupieItem("Produk Terlaris"))
        groupieAdapter.add(showPopularProductItem(datas))

        groupieAdapter.add(HeaderGroupieItem("Semua Produk"))
        groupieAdapter.add(showAllProductItem(datas))

        rvHomeContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupieAdapter
        }
    }

    private fun showBannerItem(datas:List<Product>):Group{
        val banners = datas.filter { it.discount != "0%" }
        bannerGroupieItem?.add(banners)
        return bannerGroupieItem!!
    }

    private fun showCategoryItem(datas:List<Product>):Group{
        val categories = mutableListOf<Category>()
        datas.distinctBy { it.grade }.forEach {product->
            val category = Category(product.gradeColor, product.grade)
            categories.add(category)
        }
        categoryGroupieItem?.add(categories)
        return categoryGroupieItem!!
    }

    private fun showPopularProductItem(datas: List<Product>):Group{
        val popularProducts = datas.filter { it.rating > 4 }
        popularProductGroupieItem?.add(popularProducts)
        return popularProductGroupieItem!!
    }

    private fun showAllProductItem(datas: List<Product>):Group{
        popularProductGroupieItem?.add(datas)
        return popularProductGroupieItem!!
    }

    private fun toProductDetail(data:Product){
        ProductDetailActivity.start(requireContext(), data)
    }
}
