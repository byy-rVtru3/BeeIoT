package com.app.mobile.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.ui.theme.MobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
	onEnableClick: () -> Unit,
	onDeclineClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	ModalBottomSheet(
		onDismissRequest = onDeclineClick,
		sheetState = sheetState,
		modifier = modifier
	) {
		NotificationBottomSheetContent(
			onEnableClick = onEnableClick,
			onDeclineClick = onDeclineClick
		)
	}
}

@Composable
private fun NotificationBottomSheetContent(
	onEnableClick: () -> Unit,
	onDeclineClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp)
			.padding(bottom = 32.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = Icons.Rounded.Notifications,
			contentDescription = null,
			modifier = Modifier.size(48.dp),
			tint = MaterialTheme.colorScheme.primary
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = stringResource(R.string.notifications_title),
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Center
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = stringResource(R.string.notifications_description),
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(32.dp))

		Button(
			onClick = onEnableClick,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(stringResource(R.string.turn_on_notifications))
		}

		Spacer(modifier = Modifier.height(8.dp))

		TextButton(
			onClick = onDeclineClick,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(stringResource(R.string.turn_off_notifications))
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun NotificationBottomSheetPreview() {
	MobileTheme {
		NotificationBottomSheetContent(
			onEnableClick = {},
			onDeclineClick = {}
		)
	}
}