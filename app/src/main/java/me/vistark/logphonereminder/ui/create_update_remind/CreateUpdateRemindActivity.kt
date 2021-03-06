package me.vistark.logphonereminder.ui.create_update_remind

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.common_create_update_reminder.*
import me.vistark.fastdroid_lib.ui.activities.FastdroidActivity
import me.vistark.fastdroid_lib.utils.DateTimeUtils.Companion.format
import me.vistark.fastdroid_lib.utils.FastToasty.ToastError
import me.vistark.fastdroid_lib.utils.FastToasty.ToastSuccess
import me.vistark.fastdroid_lib.utils.StringUtils.Guid
import me.vistark.fastdroid_lib.utils.ViewExtension.binDateTimePicker
import me.vistark.fastdroid_lib.utils.ViewExtension.onTap
import me.vistark.fastdroid_lib.utils.ViewExtension.onTextChanged
import me.vistark.logphonereminder.R
import me.vistark.logphonereminder.application.entities.ReminderLogEntity
import me.vistark.logphonereminder.application.repositories.ReminderLogRepository
import me.vistark.logphonereminder.ui.area_manager.AreaManagerActivity
import me.vistark.logphonereminder.ui.direct_manager.DirectManagerActivity
import java.util.*

class CreateUpdateRemindActivity : FastdroidActivity(
    R.layout.activity_create_remind,
    isHaveActionBar = true
) {
    lateinit var ReminderLogRepository: ReminderLogRepository

    var isDateTimeSelected = false

    var entity = ReminderLogEntity("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderLogRepository = ReminderLogRepository(this)

        initPreArg()
        bindDateTimePicker()
        bindAreSelector()
        bindDirectionSelector()
        bindNoteInputer()

        initOnButtonConfirm()

        initActionBar()
    }

    private fun bindNoteInputer() {
        notes.onTextChanged {
            validate()
            entity.Note = it
        }
    }

    private fun initOnButtonConfirm() {
        ceConfirmButton.onTap {
            if (ReminderLogRepository.Any(
                    "${ReminderLogEntity.PHONE_NUMBER}=? AND" +
                            " ${ReminderLogEntity.REMIND_AT}=? AND" +
                            " ${ReminderLogEntity.ID}!=?",
                    arrayOf(
                        entity.PhoneNumber,
                        entity.RemindAt.format("HH:mm:ss dd/MM/yyyy"),
                        entity.Id
                    )
                )
            ) {
                ToastSuccess("Kh??ng th??? ?????t nh???c cho c??ng m???t s??? ??i???n tho???i t???i c??ng m???t th???i ??i???m")
                return@onTap
            }
            if (entity.Id.isEmpty()) {
                createRemind()
            } else {
                updateRemind()
            }
        }
    }

    private fun createRemind() {
        entity.Id = Guid()
        val res = ReminderLogRepository.Create(entity) > 0
        if (res) {
            ToastSuccess("T???o nh???c cho s??? \"${entity.PhoneNumber}\" th??nh c??ng")
            setResult(RESULT_OK)
            finish()
        } else {
            ToastError("T???o nh???c ch??a th??nh c??ng, vui l??ng th??? l???i")
        }
    }

    private fun updateRemind() {
        val res = ReminderLogRepository.Update(entity.Id, entity) > 0
        if (res) {
            ToastSuccess("C???p nh???t nh???c cho s??? \"${entity.PhoneNumber}\" th??nh c??ng")
            setResult(RESULT_OK)
            finish()
        } else {
            ToastError("C???p nh???t nh???c ch??a th??nh c??ng, vui l??ng th??? l???i")
        }
    }

    private fun initActionBar() {
        if (entity.Id.isEmpty()) {
            supportActionBar?.title = "Th??m nh???c m???i"
            ceConfirmButton.text = "Th??m nh???c"
        } else {
            supportActionBar?.title = "S???a nh???c nh???"
            ceConfirmButton.text = "S???a nh???c"
        }

        // Hi???n th??? n??t tr??? v???F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    // Khi nh???n n??t tr??? v???
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun validate() {
        ceConfirmButton.isEnabled =
            !(entity.Area.isEmpty() || entity.Direction.isEmpty() || entity.PhoneNumber.isEmpty() || !isDateTimeSelected)
    }

    private fun bindDirectionSelector() {
        directionName.onTap {
            val intent = Intent(this, DirectManagerActivity::class.java)
            intent.putExtra("PICK", true)
            startActivityForTextResult("NAME", intent) { s ->
                if (!s.isEmpty()) {
                    directionName.text = s
                    entity.Direction = s
                    validate()
                } else {
                    ToastError("Ch???n h?????ng kh??ng th??nh c??ng")
                }
            }
        }
    }

    private fun bindAreSelector() {
        areaName.onTap {
            val intent = Intent(this, AreaManagerActivity::class.java)
            intent.putExtra("PICK", true)
            startActivityForTextResult("NAME", intent) { s ->
                if (!s.isEmpty()) {
                    areaName.text = s
                    entity.Area = s
                    validate()
                } else {
                    ToastError("Ch???n khu v???c kh??ng th??nh c??ng")
                }
            }
        }
    }

    private fun bindDateTimePicker() {
        remindAt.binDateTimePicker(entity.RemindAt) {
            isDateTimeSelected = true
            validate()
            val cal = Calendar.getInstance()
            cal.time = it
            cal.set(Calendar.SECOND, 0)
            entity.RemindAt = cal.time
            entity.IsReminded = false
        }
    }

    private fun initPreArg() {
        val gottedId = intent.getStringExtra("ID")
        val gottedPhoneNumber = intent.getStringExtra("PhoneNumber")
        if (gottedId.isNullOrEmpty()) {
            if (gottedPhoneNumber.isNullOrEmpty()) {
                finish()
                return
            } else {
                entity.PhoneNumber = gottedPhoneNumber
            }
        } else {
            val temp = ReminderLogRepository.Get(gottedId)
            if (temp != null) {
                entity = temp
                areaName.text = entity.Area
                directionName.text = entity.Direction
                isDateTimeSelected = true
                remindAt.text = entity.RemindAt.format("HH:mm:ss dd/MM/yyyy")
                notes.setText(entity.Note)
            } else {
                finish()
                return
            }
        }

        phoneNumber.setText(entity.PhoneNumber)
    }
}