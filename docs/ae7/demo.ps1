# Demostracion de la API para la defensa de Ae7.
# Requisito: aplicacion corriendo (mvn spring-boot:run).
# Uso: powershell -ExecutionPolicy Bypass -File docs\ae7\demo.ps1

$B = "http://localhost:8080/api/reservas"
$h = "Content-Type: application/json"
function Paso($titulo, [string[]]$curlArgs) {
    Write-Host ""
    Write-Host ">> $titulo" -ForegroundColor Cyan
    & curl.exe -s -w "  [HTTP %{http_code}]`n" @curlArgs
}

Paso "1. Salud de la API"                         @("$B/salud")
Paso "2. Regla de cancelacion: 2 horas (limite)"   @("$B/puede-cancelar?horas=2")
Paso "3. Regla de cancelacion: 1 hora"             @("$B/puede-cancelar?horas=1")
Paso "4. Crear reserva ESTUDIANTE"                 @("-X", "POST", $B, "-H", $h, "-d", '{\"id\":\"DEMO-1\",\"tipo\":\"ESTUDIANTE\"}')
Paso "5. Total con Strategy (base 40, -10%)"       @("$B/DEMO-1/total?base=40")
Paso "6. Confirmar la reserva"                     @("-X", "POST", "$B/DEMO-1/confirmar")
Paso "7. Consultar: queda CONFIRMADA"              @("$B/DEMO-1")
Paso "8. Id duplicado -> 409"                      @("-X", "POST", $B, "-H", $h, "-d", '{\"id\":\"DEMO-1\",\"tipo\":\"VIP\"}')
Paso "9. Campo vacio -> 400 con detalle"           @("-X", "POST", $B, "-H", $h, "-d", '{\"id\":\"\",\"tipo\":\"VIP\"}')
Paso "10. Id inexistente -> 404"                   @("$B/NO-EXISTE")
