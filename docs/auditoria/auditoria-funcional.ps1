# Auditoria funcional repetible de la API de reservas.
# Requisito: la aplicacion corriendo (mvn spring-boot:run).
# Uso: powershell -ExecutionPolicy Bypass -File docs\auditoria\auditoria-funcional.ps1

$B = "http://localhost:8080/api/reservas"
$fallas = 0

function Revisar($desc, $esperado, [string[]]$curlArgs) {
    $r = & curl.exe -s -w " [HTTP %{http_code}]" @curlArgs
    $cod = [regex]::Match($r, "HTTP (\d+)").Groups[1].Value
    $estado = if ($cod -eq "$esperado") { "OK" } else { "FALLA"; $script:fallas++ }
    $cuerpo = if ($r.Length -gt 70) { $r.Substring(0, 52) + "..." + $r.Substring($r.Length - 11) } else { $r }
    "{0,-6} {1,-40} esperado {2} -> {3}" -f $estado, $desc, $esperado, $cuerpo
}

$json = "application/json"
Revisar "GET salud"                          200 @("$B/salud")
Revisar "GET puede-cancelar horas=2 (limite)" 200 @("$B/puede-cancelar?horas=2")
Revisar "GET puede-cancelar horas=1"         200 @("$B/puede-cancelar?horas=1")
Revisar "GET puede-cancelar horas=abc"       400 @("$B/puede-cancelar?horas=abc")
Revisar "POST crear R-100 NORMAL"            201 @("-X", "POST", $B, "-H", "Content-Type: $json", "-d", '{\"id\":\"R-100\",\"tipo\":\"NORMAL\"}')
Revisar "GET R-100 (recien creado)"          200 @("$B/R-100")
Revisar "GET R-404 (inexistente)"            404 @("$B/R-404")
Revisar "POST id vacio"                      400 @("-X", "POST", $B, "-H", "Content-Type: $json", "-d", '{\"id\":\"\",\"tipo\":\"NORMAL\"}')
Revisar "POST id duplicado (limitacion)"     201 @("-X", "POST", $B, "-H", "Content-Type: $json", "-d", '{\"id\":\"R-100\",\"tipo\":\"VIP\"}')

""
if ($fallas -eq 0) { "Resultado: 9 de 9 comprobaciones OK" } else { "Resultado: $fallas comprobaciones FALLARON" }
