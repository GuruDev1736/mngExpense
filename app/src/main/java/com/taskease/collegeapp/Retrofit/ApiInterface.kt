package com.taskease.collegeapp.Retrofit

import android.credentials.CredentialDescription
import com.taskease.collegeapp.Model.Request.AddAdmissionRequestModel
import com.taskease.collegeapp.Model.Request.AddBannerRequestModel
import com.taskease.collegeapp.Model.Request.AddBatchRequestModel
import com.taskease.collegeapp.Model.Request.AddBookRequestModel
import com.taskease.collegeapp.Model.Request.AddStockRequestModel
import com.taskease.collegeapp.Model.Request.CreateBonafideRequestModel
import com.taskease.collegeapp.Model.Request.CreateDeadlineRequestModel
import com.taskease.collegeapp.Model.Request.CreateDepartmentRequestModel
import com.taskease.collegeapp.Model.Request.CreateLostReportRequestModel
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Request.CreateStoreRequestModel
import com.taskease.collegeapp.Model.Request.CreateStudentRequestModel
import com.taskease.collegeapp.Model.Request.CreateTimeTableRequestModel
import com.taskease.collegeapp.Model.Request.CreateUserRequestModel
import com.taskease.collegeapp.Model.Request.CreateYearRequestModel
import com.taskease.collegeapp.Model.Request.LoginRequestModel
import com.taskease.collegeapp.Model.Response.AddAdmissionResponseModel
import com.taskease.collegeapp.Model.Response.AddBannerResponseModel
import com.taskease.collegeapp.Model.Response.AddBatchResponseModel
import com.taskease.collegeapp.Model.Response.AddBookResponseModel
import com.taskease.collegeapp.Model.Response.AddStockResponseModel
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.Model.Response.CreateBonafideResponseModel
import com.taskease.collegeapp.Model.Response.CreateDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.CreateDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.CreateLostReportResponseModel
import com.taskease.collegeapp.Model.Response.CreateStoreResponseModel
import com.taskease.collegeapp.Model.Response.CreateStudentResponseModel
import com.taskease.collegeapp.Model.Response.CreateTimeTableResponseModel
import com.taskease.collegeapp.Model.Response.CreateYearResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBatchResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBonafideResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBooksResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllReportResponseModel
import com.taskease.collegeapp.Model.Response.GetAllRoleResponseModel
import com.taskease.collegeapp.Model.Response.GetAllStocksResponseModel
import com.taskease.collegeapp.Model.Response.GetAllStoreResponseModel
import com.taskease.collegeapp.Model.Response.GetAllUsersResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.Model.Response.LoginResponseModel
import com.taskease.collegeapp.Model.Response.ShowAllAdmissionResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.TimeTableResponseModel
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.UserUniversalResponseModel
import com.taskease.collegeapp.Model.Response.Users
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("auth/login")
    fun login(@Body model : LoginRequestModel) : Call<LoginResponseModel>

    @GET("banner/")
    fun getBanners(@Header("Authorization") token : String) : Call<BannerResponseModel>

    @GET("spreadsheet/user")
    fun getAllSpreadSheetByUser(@Header("Authorization") token : String , @Query("category") category : String , @Query("department") departmentId : Int , @Query("userId") userId : Int) : Call<SpreadSheetResponseModel>

    @GET("spreadsheet/")
    fun getAllSpreadSheetByCategoryAndDepartment(@Header("Authorization") token : String , @Query("category") category : String , @Query("department") departmentId : Int) : Call<SpreadSheetResponseModel>

    @POST("spreadsheet/create/{departmentId}/{userId}")
    fun createSpreadSheet(@Header("Authorization") token : String , @Path("departmentId") departmentId : Int , @Path("userId") userId : Int , @Body model : CreateSpreadSheetRequestModel) : Call<SingleSpreadSheetResponseModel>

    @PUT("spreadsheet/update/{sheetId}")
    fun updateSheet(@Header("Authorization") token : String , @Path("sheetId") sheetId : Int , @Body model : CreateSpreadSheetRequestModel) : Call<SingleSpreadSheetResponseModel>

    @DELETE("spreadsheet/delete/{sheetId}")
    fun deleteSheet(@Header("Authorization") token : String , @Path("sheetId") sheetId : Int) : Call<UniversalResponseModel>

    @POST("api/notifications/send")
    fun sendNotification(@Header("Authorization") token : String , @Query("title") title : String , @Query("description") description: String) : Call<UniversalResponseModel>

    @GET("department/")
    fun getAllDepartments() : Call<GetAllDepartmentResponseModel>

    @GET("spreadsheet/")
    fun getAllSpreadsheetByCategory(@Header("Authorization") token : String , @Query("category") category : String , @Query("department") departmentId : Int) : Call<SpreadSheetResponseModel>

    @GET("spreadsheet/category")
    fun getAllSpreadSheetByCategoryName(@Header("Authorization") token : String , @Query("category") category : String) : Call<SpreadSheetResponseModel>

    @POST("spreadsheet/create/budget/{userId}")
    fun createBudget(@Header("Authorization") token : String , @Path("userId") userId : Int , @Body model : CreateSpreadSheetRequestModel) : Call<SingleSpreadSheetResponseModel>

    @GET("deadline/")
    fun getAllDeadlines(@Header("Authorization") token : String) : Call<GetAllDeadlineResponseModel>

    @DELETE("deadline/{deadlineId}")
    fun deleteDeadline(@Header("Authorization") token : String , @Path("deadlineId") deadlineId : Int) : Call<UniversalResponseModel>

    @POST("deadline/create/{userId}")
    fun createDeadline(@Header("Authorization") token : String , @Path("userId") userId : Int , @Body model : CreateDeadlineRequestModel) : Call<CreateDeadlineResponseModel>

    @PUT("deadline/update/{deadlineId}")
    fun updateDeadline(@Header("Authorization") token : String , @Path("deadlineId") deadlineId : Int , @Body model : CreateDeadlineRequestModel) : Call<CreateDeadlineResponseModel>

    @POST("books/create/{userId}")
    fun createBook(@Header("Authorization") token : String , @Path("userId") userId : Int , @Body model : AddBookRequestModel) : Call<AddBookResponseModel>

    @GET("books/")
    fun getAllBooks(@Header("Authorization") token : String) : Call<GetAllBooksResponseModel>

    @PUT("books/update/{bookId}")
    fun updateBook(@Header("Authorization") token : String , @Path("bookId") bookId : Int , @Body model : AddBookRequestModel) : Call<AddBookResponseModel>

    @DELETE("books/{bookId}")
    fun deleteBook(@Header("Authorization") token : String , @Path("bookId") bookId : Int) : Call<UniversalResponseModel>

    @PUT("store/toggle-status")
    fun toggleStatus(@Header("Authorization") token : String , @Query("category") category : String) : Call<UniversalResponseModel>

    @GET("store/status")
    fun getStatus(@Header("Authorization") token : String , @Query("category") category : String) : Call<UniversalResponseModel>

    @GET("stock/")
    fun getAllStocks(@Header("Authorization") token : String) : Call<GetAllStocksResponseModel>

    @DELETE("stock/{stockId}")
    fun deleteStock(@Header("Authorization") token : String , @Path("stockId") stockId : Int) : Call<UniversalResponseModel>

    @PUT("stock/status/{stockId}")
    fun updateStockStatus(@Header("Authorization") token : String , @Path("stockId") stockId : Int) : Call<UniversalResponseModel>

    @POST("stock/add")
    fun addStock(@Header("Authorization") token : String , @Body model : AddStockRequestModel) : Call<AddStockResponseModel>

    @POST("spreadsheet/create/penalty/{userId}")
    fun createPenalty(@Header("Authorization") token : String , @Path("userId") userId : Int , @Body model : CreateSpreadSheetRequestModel) : Call<SingleSpreadSheetResponseModel>

    @GET("year/")
    fun getAllYears() : Call<GetAllYearResponseModel>

    @GET("batch/department/{departmentId}")
    fun getAllBatches(@Path("departmentId") departmentId : Int) : Call<GetAllBatchResponseModel>

    @POST("student/create/{yearId}/{departmentId}/{batchId}")
    fun addStudent(@Path("yearId") yearId : Int , @Path("departmentId") departmentId : Int , @Path("batchId") batchId : Int , @Body model : CreateStudentRequestModel ) : Call<CreateStudentResponseModel>

    @GET("user/role")
    fun getAllRoles(@Header("Authorization") token : String) : Call<GetAllRoleResponseModel>

    @GET("user/role/{roleName}")
    fun getAllUsersByRole(@Header("Authorization") token : String , @Path("roleName") roleName : String) : Call<GetAllUsersResponseModel>

    @POST("auth/super/register")
    fun createSuperAdmin(@Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>

    @POST("auth/principle/register")
    fun createPrinciple(@Header("Authorization") token : String , @Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>

    @POST("auth/faculty/register/{departmentId}")
    fun createFaculty(@Header("Authorization") token : String , @Body model : CreateUserRequestModel , @Path("departmentId") departmentId : Int) : Call<UserUniversalResponseModel>

    @POST("auth/lib/register")
    fun createLib(@Header("Authorization") token : String , @Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>
    
    @POST("auth/cooperative/register")
    fun createCooperative(@Header("Authorization") token : String , @Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>

    @POST("auth/hostel/register")
    fun createWarden(@Header("Authorization") token : String , @Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>

    @POST("auth/office/register")
    fun createOffice(@Header("Authorization") token : String , @Body model : CreateUserRequestModel) : Call<UserUniversalResponseModel>

    @GET("store/")
    fun getAllStore(@Header("Authorization") token : String) : Call<GetAllStoreResponseModel>

    @PUT("store/toggle-status")
    fun updateStoreStatus(@Header("Authorization") token : String , @Query("category") category : String) : Call<UniversalResponseModel>

    @DELETE("store/")
    fun deleteStore(@Header("Authorization") token : String , @Query("category") category : String) : Call<UniversalResponseModel>

    @POST("store/add")
    fun createStore(@Header("Authorization") token : String , @Body model : CreateStoreRequestModel) : Call<CreateStoreResponseModel>

    @GET("timetable/category")
    fun getAllTimeTableByCategory(@Header("Authorization") token : String , @Query("category") category: String) : Call<TimeTableResponseModel>

    @DELETE("timetable/{id}")
    fun deleteTimeTable(@Header("Authorization") token : String , @Path("id") id: Int) : Call<UniversalResponseModel>

    @POST("timetable/create")
    fun createTimeTable(@Header("Authorization") token : String , @Body model : CreateTimeTableRequestModel , @Query("year") yearId: Int , @Query("department") departmentId: Int , @Query("batch") batchId: Int) : Call<CreateTimeTableResponseModel>

    @PUT("timetable/update/{id}")
    fun updateTimeTable(@Header("Authorization") token : String , @Path("id") id: Int , @Body model : CreateTimeTableRequestModel) : Call<CreateTimeTableResponseModel>

    @POST("banner/create")
    fun createBanner(@Header("Authorization") token : String , @Body model : AddBannerRequestModel) : Call<AddBannerResponseModel>

    @DELETE("banner/{id}")
    fun deleteBanner(@Header("Authorization") token : String , @Path("id") bannerId : Int) : Call<UniversalResponseModel>

    @POST("year/create")
    fun createYear(@Header("Authorization") token : String , @Body model : CreateYearRequestModel) : Call<CreateYearResponseModel>

    @POST("department/create")
    fun createDepartment(@Header("Authorization") token : String , @Body model : CreateDepartmentRequestModel) : Call<CreateDepartmentResponseModel>

    @DELETE("year/{id}")
    fun deleteYear(@Header("Authorization") token : String , @Path("id") yearId : Int) : Call<UniversalResponseModel>

    @DELETE("department/{id}")
    fun deleteDepartment(@Header("Authorization") token : String , @Path("id") departmentId : Int) : Call<UniversalResponseModel>

    @POST("batch/create/{departmentId}")
    fun createBatch(@Header("Authorization") token : String , @Path("departmentId") departmentId : Int , @Body model : AddBatchRequestModel) : Call<AddBatchResponseModel>

    @DELETE("batch/{batchId}")
    fun deleteBatch(@Header("Authorization") token : String , @Path("batchId") batchId : Int) : Call<UniversalResponseModel>

    @GET("timetable/details")
    fun getTimeTableByAllDetails(@Header("Authorization") token : String , @Query("year") yearId: Int , @Query("department") departmentId: Int , @Query("batch") batchId: Int , @Query("category") category: String) : Call<TimeTableResponseModel>

    @GET("report/")
    fun getAllReport(@Header("Authorization") token : String) : Call<GetAllReportResponseModel>

    @POST("report/create/{studentId}")
    fun createLostReport(@Header("Authorization") token : String , @Body model : CreateLostReportRequestModel , @Path("studentId") studentId : Int ) : Call<CreateLostReportResponseModel>

    @GET("report/student/{studentId}")
    fun getAllReportByStudent(@Header("Authorization") token : String , @Path("studentId") studentId : Int) : Call<GetAllReportResponseModel>

    @DELETE("report/{reportId}")
    fun deleteReport(@Header("Authorization") token : String , @Path("reportId") reportId : Int) : Call<UniversalResponseModel>

    @PUT("report/update/{reportId}")
    fun updateReport(@Header("Authorization") token : String , @Path("reportId") reportId : Int , @Body model : CreateLostReportRequestModel) : Call<CreateLostReportResponseModel>

    @POST("admission/{studentId}")
    fun addAdmission(@Header("Authorization") token : String , @Path("studentId") studentId : Int , @Body model : AddAdmissionRequestModel) : Call<AddAdmissionResponseModel>

    @GET("admission/")
    fun getAllAdmission(@Header("Authorization") token : String) : Call<ShowAllAdmissionResponseModel>

    @PUT("admission/approve/{id}")
    fun approveAdmission(@Header("Authorization") token : String , @Path("id") id : Int) : Call<UniversalResponseModel>

    @PUT("admission/reject/{id}")
    fun rejectAdmission(@Header("Authorization") token : String , @Path("id") id : Int) : Call<UniversalResponseModel>

    @GET("bonafide/")
    fun getAllBonafide(@Header("Authorization") token : String) : Call<GetAllBonafideResponseModel>

    @DELETE("bonafide/{id}")
    fun deleteBonafide(@Header("Authorization") token : String , @Path("id") id : Int) : Call<UniversalResponseModel>

    @GET("bonafide/student/{studentId}")
    fun getBonafideByStudent(@Header("Authorization") token : String , @Path("studentId") studentId : Int) : Call<GetAllBonafideResponseModel>

    @POST("bonafide/create/{studentId}")
    fun createBonafide(@Header("Authorization") token : String , @Path("studentId") studentId : Int , @Body model : CreateBonafideRequestModel) : Call<CreateBonafideResponseModel>

    @POST("auth/send-otp")
    fun sendOTP(@Query("email") email : String) : Call<UniversalResponseModel>

    @POST("auth/validate-otp")
    fun validateOTP(@Query("email") email : String , @Query("otp") otp : String) : Call<UniversalResponseModel>

    @PUT("auth/changePassword")
    fun changePassword(@Query("email") email : String , @Query("password") password : String) : Call<UniversalResponseModel>

}
