@echo off
REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sList" /t "REG_SZ" /d ","

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "iCurrDigits" /t "REG_SZ" /d "0"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sThousand" /t "REG_SZ" /d ","

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sShortDate" /t "REG_SZ" /d "dd/MM/yyyy"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sNativeDigits" /t "REG_SZ" /d "0123456789"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sDecimal" /t "REG_SZ" /d "."

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "iDigits" /t "REG_SZ" /d "0"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sMonDecimalSep" /t "REG_SZ" /d "."

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "sCurrency" /t "REG_SZ" /d "$"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "iFirstDayOfWeek" /t "REG_SZ" /d "5"

REG ADD "HKEY_CURRENT_USER\Control Panel\International" /f /v "iNegCurr" /t "REG_SZ" /d "0"
cls
@echo.
@echo Set separator to comma.
@echo Set 0 for numbers after decimal.
@echo Set thousand separator to comma.
@echo Set date structure to dd/MM/yyyy.
@echo Set showing numbers in English format.
@echo Set decimal to dot.

@echo.
@echo "Your setting of region has been done to Please restart Excel program."
@echo.
@echo "Create By Mahmoud Bani Asadi, Instagram: SoftwareTrain, Website: www.arshad-hesabdar.ir"

Pause




