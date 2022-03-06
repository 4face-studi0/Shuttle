package shuttle.predictions.presentation.mapper

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import shuttle.apps.domain.model.SuggestedAppModel
import shuttle.predictions.presentation.model.WidgetAppUiModel
import shuttle.util.android.GetIconForApp
import shuttle.util.android.GetLaunchIntentForApp

class WidgetAppUiModelMapper(
    private val getIconForApp: GetIconForApp,
    private val getLaunchIntentForApp: GetLaunchIntentForApp
) {

    fun toUiModel(appModel: SuggestedAppModel) = WidgetAppUiModel(
        id = appModel.id,
        name = appModel.name.value,
        icon = getIconForApp(appModel.id).setTint(appModel.isSuggested),
        launchIntent = getLaunchIntentForApp(appModel.id)
    )

    fun toUiModels(appModels: Collection<SuggestedAppModel>): List<WidgetAppUiModel> =
        appModels.map(::toUiModel)

    private fun Icon.setTint(isSuggested: Boolean) = apply {
        if (isSuggested.not()) {
            setTint(Color.parseColor("#B3CCCCCC"))
            setTintMode(PorterDuff.Mode.SRC_ATOP)
        }
    }
}
