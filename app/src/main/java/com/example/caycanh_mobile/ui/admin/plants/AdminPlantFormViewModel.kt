package com.example.caycanh_mobile.ui.admin.plants

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.remote.dto.admin.*
import com.example.caycanh_mobile.data.repository.AdminRepository
import com.example.caycanh_mobile.data.repository.CategoryRepository
import com.example.caycanh_mobile.data.repository.PlantRepository
import com.example.caycanh_mobile.util.ImageFileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Ảnh trong form: cũ (có URL) hoặc mới (Uri chưa upload)
sealed class FormImage {
    abstract val isPrimary: Boolean
    data class Existing(val url: String, override val isPrimary: Boolean) : FormImage()
    data class Local(val uri: Uri, override val isPrimary: Boolean) : FormImage()
}

data class AdminPlantFormUiState(
    val isEdit: Boolean = false,
    val plantId: String? = null,
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryId: String? = null,
    val name: String = "",
    val description: String = "",
    val listingType: String = "sale",     // sale | rent | both
    val priceSale: String = "",
    val pricePerDay: String = "",
    val pricePerWeek: String = "",
    val pricePerMonth: String = "",
    val stockQuantity: String = "0",
    val rentAvailableQty: String = "0",
    val images: List<FormImage> = emptyList(),
    val isLoading: Boolean = false,        // load dữ liệu khi sửa
    val isSaving: Boolean = false,
    val uploadProgress: String? = null,    // "Đang tải ảnh 2/5..."
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    // dialog thêm danh mục nhanh
    val showAddCategoryDialog: Boolean = false,
    val newCategoryName: String = "",
    val isCreatingCategory: Boolean = false
)

@HiltViewModel
class AdminPlantFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plantRepository: PlantRepository,
    private val adminRepository: AdminRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val plantIdArg: String? = savedStateHandle["plantId"]

    private val _uiState = MutableStateFlow(
        AdminPlantFormUiState(isEdit = plantIdArg != null, plantId = plantIdArg)
    )
    val uiState: StateFlow<AdminPlantFormUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (plantIdArg != null) loadPlant(plantIdArg)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().onSuccess { list ->
                _uiState.update { st ->
                    st.copy(
                        categories = list,
                        // nếu chưa chọn và là tạo mới, chọn danh mục đầu
                        selectedCategoryId = st.selectedCategoryId ?: list.firstOrNull()?.id
                    )
                }
            }
        }
    }

    private fun loadPlant(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            plantRepository.getPlantById(id)
                .onSuccess { p ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedCategoryId = p.categoryId,
                            name = p.name,
                            description = p.description ?: "",
                            listingType = p.listingType,
                            priceSale = p.priceSale?.toString() ?: "",
                            pricePerDay = p.pricePerDay?.toString() ?: "",
                            pricePerWeek = p.pricePerWeek?.toString() ?: "",
                            pricePerMonth = p.pricePerMonth?.toString() ?: "",
                            stockQuantity = p.stockQuantity.toString(),
                            rentAvailableQty = p.rentAvailableQty.toString(),
                            // map ảnh cũ — dùng imageDetails nếu PlantResponse có, ở đây dùng imageUrls
                            images = p.imageUrls.mapIndexed { idx, url ->
                                FormImage.Existing(url = url, isPrimary = idx == 0)
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    // ── Field setters ──
    fun onCategorySelect(id: String) { _uiState.update { it.copy(selectedCategoryId = id) } }
    fun onNameChange(v: String) { _uiState.update { it.copy(name = v) } }
    fun onDescriptionChange(v: String) { _uiState.update { it.copy(description = v) } }
    fun onListingTypeChange(v: String) { _uiState.update { it.copy(listingType = v) } }
    fun onPriceSaleChange(v: String) { _uiState.update { it.copy(priceSale = v.filter { c -> c.isDigit() }) } }
    fun onPriceDayChange(v: String) { _uiState.update { it.copy(pricePerDay = v.filter { c -> c.isDigit() }) } }
    fun onPriceWeekChange(v: String) { _uiState.update { it.copy(pricePerWeek = v.filter { c -> c.isDigit() }) } }
    fun onPriceMonthChange(v: String) { _uiState.update { it.copy(pricePerMonth = v.filter { c -> c.isDigit() }) } }
    fun onStockChange(v: String) { _uiState.update { it.copy(stockQuantity = v.filter { c -> c.isDigit() }.ifEmpty { "0" }) } }
    fun onRentQtyChange(v: String) { _uiState.update { it.copy(rentAvailableQty = v.filter { c -> c.isDigit() }.ifEmpty { "0" }) } }

    // ── Ảnh ──
    fun addImages(uris: List<Uri>) {
        _uiState.update { st ->
            val newImages = uris.map { FormImage.Local(it, isPrimary = false) }
            val combined = st.images + newImages
            // nếu chưa có ảnh primary nào, đặt ảnh đầu làm primary
            val hasPrimary = combined.any { it.isPrimary }
            val fixed = if (!hasPrimary && combined.isNotEmpty()) {
                combined.mapIndexed { idx, img -> setPrimaryFlag(img, idx == 0) }
            } else combined
            st.copy(images = fixed)
        }
    }

    fun removeImage(index: Int) {
        _uiState.update { st ->
            val list = st.images.toMutableList()
            if (index !in list.indices) return@update st
            val wasPrimary = list[index].isPrimary
            list.removeAt(index)
            // nếu xóa ảnh primary, đặt ảnh đầu còn lại làm primary
            val fixed = if (wasPrimary && list.isNotEmpty()) {
                list.mapIndexed { idx, img -> setPrimaryFlag(img, idx == 0) }
            } else list
            st.copy(images = fixed)
        }
    }

    fun setPrimary(index: Int) {
        _uiState.update { st ->
            st.copy(images = st.images.mapIndexed { idx, img -> setPrimaryFlag(img, idx == index) })
        }
    }

    private fun setPrimaryFlag(img: FormImage, primary: Boolean): FormImage = when (img) {
        is FormImage.Existing -> img.copy(isPrimary = primary)
        is FormImage.Local -> img.copy(isPrimary = primary)
    }

    // ── Dialog thêm danh mục ──
    fun openAddCategory() { _uiState.update { it.copy(showAddCategoryDialog = true, newCategoryName = "") } }
    fun dismissAddCategory() { _uiState.update { it.copy(showAddCategoryDialog = false) } }
    fun onNewCategoryNameChange(v: String) { _uiState.update { it.copy(newCategoryName = v) } }

    fun confirmAddCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingCategory = true) }
            categoryRepository.createCategory(name)
                .onSuccess { cat ->
                    _uiState.update {
                        it.copy(
                            isCreatingCategory = false,
                            showAddCategoryDialog = false,
                            categories = it.categories + cat,
                            selectedCategoryId = cat.id    // tự chọn danh mục vừa tạo
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCreatingCategory = false, errorMessage = e.message) }
                }
        }
    }

    fun consumeError() { _uiState.update { it.copy(errorMessage = null) } }

    // ── Lưu ──
    fun save(context: Context) {
        val st = _uiState.value
        // Validate cơ bản
        if (st.selectedCategoryId == null) { setError("Vui lòng chọn danh mục"); return }
        if (st.name.isBlank()) { setError("Vui lòng nhập tên cây"); return }
        val isSale = st.listingType == "sale" || st.listingType == "both"
        val isRent = st.listingType == "rent" || st.listingType == "both"
        if (isSale && st.priceSale.isBlank()) { setError("Nhập giá bán"); return }
        if (isRent && (st.pricePerDay.isBlank() || st.pricePerWeek.isBlank() || st.pricePerMonth.isBlank())) {
            setError("Nhập đủ 3 giá thuê (ngày/tuần/tháng)"); return
        }
        if (st.images.isEmpty()) { setError("Thêm ít nhất 1 ảnh"); return }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // 1. Upload các ảnh Local, giữ URL ảnh Existing — theo đúng thứ tự
            val localImages = st.images.filterIsInstance<FormImage.Local>()
            var uploadedCount = 0
            val urlMap = HashMap<Uri, String>()
            for (local in localImages) {
                _uiState.update {
                    it.copy(uploadProgress = "Đang tải ảnh ${uploadedCount + 1}/${localImages.size}...")
                }
                val file = ImageFileUtil.uriToTempFile(context, local.uri)
                if (file == null) {
                    _uiState.update { it.copy(isSaving = false, uploadProgress = null, errorMessage = "Không đọc được ảnh") }
                    return@launch
                }
                val result = adminRepository.uploadImage(file)
                file.delete()
                result.onSuccess { url -> urlMap[local.uri] = url }
                    .onFailure { e ->
                        _uiState.update { it.copy(isSaving = false, uploadProgress = null, errorMessage = "Lỗi tải ảnh: ${e.message}") }
                        return@launch
                    }
                uploadedCount++
            }

            // 2. Gom mảng images[] theo thứ tự hiển thị
            val payload = st.images.mapIndexed { idx, img ->
                val url = when (img) {
                    is FormImage.Existing -> img.url
                    is FormImage.Local -> urlMap[img.uri] ?: ""
                }
                PlantImagePayload(imageUrl = url, isPrimary = img.isPrimary, sortOrder = idx)
            }.filter { it.imageUrl.isNotBlank() }

            _uiState.update { it.copy(uploadProgress = "Đang lưu cây...") }

            // 3. Gọi tạo/sửa
            val result = if (st.isEdit && st.plantId != null) {
                adminRepository.updatePlant(
                    st.plantId,
                    UpdatePlantRequest(
                        categoryId = st.selectedCategoryId,
                        name = st.name.trim(),
                        description = st.description.ifBlank { null },
                        listingType = st.listingType,
                        priceSale = if (isSale) st.priceSale.toLongOrNull() else null,
                        pricePerDay = if (isRent) st.pricePerDay.toLongOrNull() else null,
                        pricePerWeek = if (isRent) st.pricePerWeek.toLongOrNull() else null,
                        pricePerMonth = if (isRent) st.pricePerMonth.toLongOrNull() else null,
                        stockQuantity = st.stockQuantity.toIntOrNull() ?: 0,
                        rentAvailableQty = st.rentAvailableQty.toIntOrNull() ?: 0,
                        images = payload
                    )
                )
            } else {
                adminRepository.createPlant(
                    CreatePlantRequest(
                        categoryId = st.selectedCategoryId,
                        name = st.name.trim(),
                        description = st.description.ifBlank { null },
                        listingType = st.listingType,
                        priceSale = if (isSale) st.priceSale.toLongOrNull() else null,
                        pricePerDay = if (isRent) st.pricePerDay.toLongOrNull() else null,
                        pricePerWeek = if (isRent) st.pricePerWeek.toLongOrNull() else null,
                        pricePerMonth = if (isRent) st.pricePerMonth.toLongOrNull() else null,
                        stockQuantity = st.stockQuantity.toIntOrNull() ?: 0,
                        rentAvailableQty = st.rentAvailableQty.toIntOrNull() ?: 0,
                        images = payload
                    )
                )
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false, uploadProgress = null, saveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, uploadProgress = null, errorMessage = e.message) }
            }
        }
    }

    private fun setError(msg: String) { _uiState.update { it.copy(errorMessage = msg) } }
}