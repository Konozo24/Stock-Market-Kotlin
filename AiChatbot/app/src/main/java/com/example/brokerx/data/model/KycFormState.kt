data class KycFormState(
    val fullName: String = "",
    val dob: String = "",
    val gender: String = "",
    val nationality: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val idType: String = "",
    val idNumber: String = "",
    val idExpiry: String = "",
    val occupation: String = "",
    val sourceOfFunds: String = "",
    val incomeBracket: String = "",
    val tin: String = "",
    val investmentExperience: String = "",
    val agreedToTerms: Boolean = false
)