package cz.smycka.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cz.smycka.example.R
import kiwi.orbit.compose.icons.Icons
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.ButtonPrimary
import kiwi.orbit.compose.ui.controls.CircularProgressIndicator
import kiwi.orbit.compose.ui.controls.Icon
import kiwi.orbit.compose.ui.controls.Scaffold
import kiwi.orbit.compose.ui.controls.Text
import kiwi.orbit.compose.ui.controls.TextField

const val signInNavigationRoute = "signIn"

fun NavGraphBuilder.signInScreen(onNavigateToPicture: (String) -> Unit) {
    composable(route = signInNavigationRoute) {
        SignInRoute(onNavigateToPicture = onNavigateToPicture)
    }
}

@Composable
fun SignInRoute(
    onNavigateToPicture: (String) -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.signInUiState.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()

    SignInScreen(
        uiState = uiState,
        userName = userName,
        onUserNameChange = remember { viewModel::onUserNameChange },
        password = password,
        onPasswordChange = remember { viewModel::onPasswordChange },
        onSignIn = remember { viewModel::signIn }
    )
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.signInEvents.collect {
                when (it) {
                    is SignInEvents.NavigateToPictureScreen -> {
                        onNavigateToPicture(it.userName)
                        viewModel.clearResult()
                    }
                }
            }
        }
    }
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    userName: String,
    onUserNameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit
) {
    val uneditableStates = remember { setOf(SignInUiState.Success, SignInUiState.Loading) }
    var userNameHasError by rememberSaveable { mutableStateOf(false) }
    var passwordHasError by rememberSaveable { mutableStateOf(false) }
    val onSignInCheck: () -> Unit = {
        if (userName.isEmpty() || userName.any { it.isUpperCase() }) {
            userNameHasError = true
        }
        if (password.isEmpty() || password.any { it.isUpperCase() }) {
            passwordHasError = true
        }
        if (!userNameHasError && !passwordHasError) {
            onSignIn()
        }
    }

    Scaffold(
        action = {
            SignInActions(
                showButton = uiState !in uneditableStates,
                showProgress = uiState is SignInUiState.Loading,
                onSignInClick = onSignInCheck
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                text = stringResource(id = R.string.app_name),
                style = OrbitTheme.typography.title1,
                fontSize = TextUnit(48f, TextUnitType.Sp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.sign_in),
                    style = OrbitTheme.typography.title1
                )

                UserNameTextField(
                    userName = userName,
                    onUserNameChange = onUserNameChange,
                    enabled = uiState !is SignInUiState.Loading,
                    userNameHasError = userNameHasError,
                    onUserNameErrorChange = { userNameHasError = it }
                )

                PasswordTextField(
                    password = password,
                    onPasswordChange = onPasswordChange,
                    enabled = uiState !is SignInUiState.Loading,
                    passwordHasError = passwordHasError,
                    onPasswordHasErrorChange = { passwordHasError = it },
                    onDone = onSignInCheck
                )
                ResultRow(uiState = uiState)
            }
        }
    }
}

@Composable
private fun ResultRow(uiState: SignInUiState) {
    val resultStates = remember {
        setOf(
            SignInUiState.Success,
            SignInUiState.Error,
            SignInUiState.WrongUserNameOrPassword,
            SignInUiState.NoInternet
        )
    }
    if (uiState in resultStates) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.padding(8.dp),
                painter = when (uiState) {
                    SignInUiState.Success -> Icons.Check
                    SignInUiState.Error -> Icons.Alert
                    SignInUiState.NoInternet,
                    SignInUiState.WrongUserNameOrPassword -> Icons.AlertCircle

                    else -> Icons.Placeholder
                },
                tint = when (uiState) {
                    SignInUiState.Success -> OrbitTheme.colors.success
                    SignInUiState.WrongUserNameOrPassword -> OrbitTheme.colors.warning
                    else -> OrbitTheme.colors.critical
                }.normal,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(
                    id = when (uiState) {
                        SignInUiState.Error -> R.string.error_general
                        SignInUiState.Success -> R.string.success
                        SignInUiState.WrongUserNameOrPassword -> R.string.error_unauthorized
                        SignInUiState.NoInternet -> R.string.error_no_internet
                        else -> R.string.placeholder
                    }
                ),
                color = when (uiState) {
                    SignInUiState.Success -> OrbitTheme.colors.success
                    SignInUiState.WrongUserNameOrPassword -> OrbitTheme.colors.warning
                    else -> OrbitTheme.colors.critical
                }.normal
            )
        }
    }
}

@Composable
private fun SignInActions(
    showButton: Boolean,
    showProgress: Boolean,
    onSignInClick: () -> Unit
) {
    if (showButton) {
        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("Confirm"),
            onClick = onSignInClick
        ) {
            Text(text = stringResource(id = R.string.sign_in))
        }
    }
    if (showProgress) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp).testTag("progress")
            )
        }
    }
}

@Composable
private fun UserNameTextField(
    userName: String,
    onUserNameChange: (String) -> Unit,
    enabled: Boolean,
    userNameHasError: Boolean,
    onUserNameErrorChange: (Boolean) -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .testTag("userName"),
        value = userName,
        onValueChange = {
            onUserNameChange(it)
            onUserNameErrorChange(it.isEmpty() || it.any { character -> character.isUpperCase() })
        },
        error = if (userNameHasError) {
            { LowerCapNonEmptyErrorLabel(userName) }
        } else {
            null
        },
        label = { Text(text = stringResource(id = R.string.username)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        leadingIcon = {
            Icon(
                Icons.Passenger,
                contentDescription = null
            )
        },
        enabled = enabled
    )
}

@Composable
private fun LowerCapNonEmptyErrorLabel(userName: String) {
    Text(
        text = stringResource(
            id = if (userName.isEmpty()) {
                R.string.error_empty_input
            } else {
                R.string.error_lower_caps_only
            }
        )
    )
}

@Composable
private fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    enabled: Boolean,
    passwordHasError: Boolean,
    onPasswordHasErrorChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .testTag("password"),
        value = password,
        onValueChange = {
            onPasswordChange(it)
            onPasswordHasErrorChange(it.isEmpty())
        },
        error = if (passwordHasError) {
            { LowerCapNonEmptyErrorLabel(password) }
        } else {
            null
        },
        label = { Text(text = stringResource(id = R.string.password)) },
        leadingIcon = {
            Icon(
                painter = Icons.Security,
                contentDescription = null
            )
        },
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        enabled = enabled,
        trailingIcon = {
            Icon(
                painter = if (showPassword) {
                    Icons.Visibility
                } else {
                    Icons.VisibilityOff
                },
                contentDescription = null
            )
        },
        onTrailingIconClick = { showPassword = !showPassword }
    )
}

@Preview
@Composable
private fun PreviewSignInScreen() {
    OrbitTheme {
        SignInScreen(
            uiState = SignInUiState.None,
            userName = "john_doe",
            onUserNameChange = {},
            password = "pwd",
            onPasswordChange = {},
            onSignIn = {}
        )
    }
}

@Preview
@Composable
private fun PreviewSignInScreenLoading() {
    OrbitTheme {
        SignInScreen(
            uiState = SignInUiState.Loading,
            userName = "john_doe",
            onUserNameChange = {},
            password = "pwd",
            onPasswordChange = {},
            onSignIn = {}
        )
    }
}

@Preview
@Composable
private fun PreviewSignInScreenSuccess() {
    OrbitTheme {
        SignInScreen(
            uiState = SignInUiState.Success,
            userName = "john_doe",
            onUserNameChange = {},
            password = "pwd",
            onPasswordChange = {},
            onSignIn = {}
        )
    }
}

@Preview
@Composable
private fun PreviewSignInScreenUnauthorized() {
    OrbitTheme {
        SignInScreen(
            uiState = SignInUiState.WrongUserNameOrPassword,
            userName = "john_doe",
            onUserNameChange = {},
            password = "pwd",
            onPasswordChange = {},
            onSignIn = {}
        )
    }
}

@Preview
@Composable
private fun PreviewSignInScreenError() {
    OrbitTheme {
        SignInScreen(
            uiState = SignInUiState.Error,
            userName = "john_doe",
            onUserNameChange = {},
            password = "pwd",
            onPasswordChange = {},
            onSignIn = {}
        )
    }
}
