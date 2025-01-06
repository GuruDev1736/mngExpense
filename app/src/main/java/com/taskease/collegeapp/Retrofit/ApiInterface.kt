package com.taskease.collegeapp.Retrofit

import android.credentials.CredentialDescription
import com.taskease.collegeapp.Model.Request.AddBookRequestModel
import com.taskease.collegeapp.Model.Request.AddStockRequestModel
import com.taskease.collegeapp.Model.Request.CreateDeadlineRequestModel
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Request.CreateStudentRequestModel
import com.taskease.collegeapp.Model.Request.LoginRequestModel
import com.taskease.collegeapp.Model.Response.AddBookResponseModel
import com.taskease.collegeapp.Model.Response.AddStockResponseModel
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.Model.Response.CreateDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.CreateStudentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBatchResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBooksResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllRoleResponseModel
import com.taskease.collegeapp.Model.Response.GetAllStocksResponseModel
import com.taskease.collegeapp.Model.Response.GetAllUsersResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.Model.Response.LoginResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
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
}