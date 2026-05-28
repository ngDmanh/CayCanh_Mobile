package com.example.caycanh_mobile.ui.customer.profile

data class ProfileEditUiState(
    val email: String = "",
    val initialFullName: String = "",
    val initialPhone: String = "",

    // Current input values
    val fullName: String = "",
    val phone: String = "",

    // Validation errors
    val fullNameError: String? = null,
    val phoneError: String? = null,

    // State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
) {
    val hasChanges: Boolean
        get() = fullName != initialFullName || phone != initialPhone

    val canSave: Boolean
        get() = hasChanges && fullName.isNotBlank() && !isSaving
}