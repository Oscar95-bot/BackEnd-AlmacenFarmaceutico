package com.almacen.farmaceutico.AlmacenFarmaceutico;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Duration;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventarioApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired InsumoRepository insumos;
    @Autowired LoteRepository lotes;
    @Autowired PasswordEncoder encoder;
    @Autowired InsumoService insumoService;
    @Autowired MovimientoService movimientoService;
    @Autowired CatalogoRepository catalogos;
    @Autowired SecuenciaCodigoRepository secuencias;
    @Autowired ProveedorRepository proveedores;
    @Autowired ClienteRepository clientes;
    @Autowired JwtService jwtService;
    Long categoriaId, unidadId, presentacionId, entradaId, cargaId, salidaId, mermaId, vencimientoId;
    Long proveedorId;
    Long clienteId, internoId, devolucionId;

    @BeforeEach
    void preparar() {
        jdbc.update("delete from movimientos");
        jdbc.update("delete from lotes");
        jdbc.update("delete from insumos");
        jdbc.update("delete from usuarios");
        jdbc.update("delete from proveedores");
        jdbc.update("delete from clientes");
        jdbc.update("delete from catalogos");
        jdbc.update("delete from secuencias_codigo");
        secuencias.save(new SecuenciaCodigo("INSUMO", 0));
        categoriaId = opcion(TipoCatalogo.CATEGORIA, "MEDICAMENTOS", "Medicamentos", false);
        unidadId = opcion(TipoCatalogo.UNIDAD_MEDIDA, "CAJA", "Caja", false);
        presentacionId = opcion(TipoCatalogo.PRESENTACION, "TABLETAS", "Tabletas", false);
        entradaId = opcion(TipoCatalogo.MOTIVO_ENTRADA, "COMPRA", "Compra a proveedor", false);
        cargaId = opcion(TipoCatalogo.MOTIVO_ENTRADA, "CARGA_INICIAL", "Carga inicial de inventario", false);
        salidaId = opcion(TipoCatalogo.MOTIVO_SALIDA, "DESPACHO", "Despacho a farmacia o clínica", false);
        mermaId = opcion(TipoCatalogo.MOTIVO_SALIDA, "MERMA", "Baja por merma", true);
        vencimientoId = opcion(TipoCatalogo.MOTIVO_SALIDA, "VENCIMIENTO", "Baja por vencimiento", true);
        devolucionId = opcion(TipoCatalogo.MOTIVO_SALIDA, "DEVOLUCION_PROVEEDOR", "Devolución a proveedor", false);
        proveedorId = proveedores.save(new Proveedor("Proveedor de prueba", "20000000001", "Dirección ficticia 101")).getId();
        clienteId = clientes.save(new Cliente("FARMACIA", "Farmacia de prueba", "20000000011", "Dirección ficticia 110", TipoCliente.CLIENTE)).getId();
        internoId = clientes.save(new Cliente("BAJAS", "Área de bajas", null, "Zona interna de prueba", TipoCliente.INTERNO)).getId();
        usuarios.save(new Usuario("Administrador de prueba", "admin", encoder.encode("clave-prueba"), Rol.ADMINISTRADOR));
        usuarios.save(new Usuario("Operario de prueba", "operario", encoder.encode("clave-prueba"), Rol.OPERATIVO));
        usuarios.save(new Usuario("Logística de prueba", "logistica", encoder.encode("clave-prueba"), Rol.LOGISTICA));
        usuarios.save(new Usuario("Almacenero de prueba", "almacenero", encoder.encode("clave-prueba"), Rol.ALMACENERO));
    }

    @Test
    void inventarioRequiereSesion() throws Exception {
        mvc.perform(get("/api/insumos")).andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerYDocumentacionOpenApiSonPublicosSinExponerInventario() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/swagger-ui/index.html")));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
        mvc.perform(get("/swagger-ui/swagger-ui.css")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/insumos']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());
        mvc.perform(get("/api/insumos")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginEmiteJwtYLogoutLoInvalidaSinCrearSesion() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuario":"admin","contrasena":"clave-prueba"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists()).andReturn();
        assertThat(login.getRequest().getSession(false)).isNull();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/api/auth/perfil").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/insumos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(insumoJson(0)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/perfil").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/perfil")).andExpect(status().isUnauthorized());
    }

    @Test
    void jwtManipuladoVencidoYDeOtraFirmaSonRechazados() throws Exception {
        String valido = jwtService.emitir(UsuarioResponse.from(usuarios.findByUsuario("admin").orElseThrow())).token();
        String[] partes = valido.split("\\.");
        String manipulado = partes[0] + "." + java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"admin\",\"rol\":\"ADMINISTRADOR\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8)) + "." + partes[2];
        String otraFirma = new JwtService("", Duration.ofMinutes(30))
                .emitir(UsuarioResponse.from(usuarios.findByUsuario("admin").orElseThrow())).token();
        for (String token : new String[]{"invalido", manipulado, otraFirma}) {
            mvc.perform(get("/api/insumos").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        // El propio servicio también debe rechazar tokens caducados.
        JwtService breve = new JwtService("", Duration.ofMillis(1));
        String vencido = breve.emitir(UsuarioResponse.from(usuarios.findByUsuario("admin").orElseThrow())).token();
        Thread.sleep(20);
        assertThatThrownBy(() -> breve.validar(vencido)).isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
    }

    @Test
    void unJwtExistenteReflejaCambiosDeRolYBajaDelUsuario() throws Exception {
        Usuario usuario = usuarios.findByUsuario("operario").orElseThrow();
        String token = jwtService.emitir(UsuarioResponse.from(usuario)).token();
        mvc.perform(post("/api/insumos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0))).andExpect(status().isCreated());
        jdbc.update("update usuarios set rol = 'LOGISTICA' where id = ?", usuario.getId());
        mvc.perform(post("/api/insumos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0))).andExpect(status().isForbidden());
        jdbc.update("update usuarios set activo = false where id = ?", usuario.getId());
        mvc.perform(get("/api/insumos").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginIncorrectoEsRechazadoYLoginCorrectoNoRequiereTokenPrevio() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"admin\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token-vencido")
                        .content("{\"usuario\":\"admin\",\"contrasena\":\"clave-prueba\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void escriturasSinSesionSiguenRequiriendoLogin() throws Exception {
        mvc.perform(post("/api/insumos").contentType(MediaType.APPLICATION_JSON).content(insumoJson(0)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/movimientos/entrada").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        assertThat(insumos.count()).isZero();
    }

    @Test
    void rolesRestringenEscrituraYAdministracion() throws Exception {
        mvc.perform(post("/api/insumos").with(user("logistica"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").with(user("operario"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/inventario.csv").with(user("operario"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/insumos").with(user("logistica"))).andExpect(status().isOk());
    }

    @Test
    void crearGeneraCodigosUnicosYValidaValoresNegativos() throws Exception {
        mvc.perform(post("/api/insumos").with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("Agotado"))
                .andExpect(jsonPath("$.codigo").value("FAR-000001"));
        mvc.perform(post("/api/insumos").with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigo").value("FAR-000002"));
        mvc.perform(post("/api/insumos").with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(-1)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.stockActual").exists());
        mvc.perform(post("/api/insumos").with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(0).replace("12.50", "-1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockInicialCreaLoteYMovimientoAuditado() {
        InsumoResponse insumo = crearConStock(15);
        assertThat(insumo.stockActual()).isEqualTo(15);
        assertThat(insumo.estado()).isEqualTo("Disponible");
        var historial = movimientoService.listar(null, null, null, insumo.id(), null);
        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().responsable()).isEqualTo("Administrador de prueba");
        assertThat(insumoService.listarLotes(insumo.id()).getFirst().stockActual()).isEqualTo(15);
    }

    @Test
    void stockInicialSinLoteRevierteCreacion() {
        var request = new InsumoRequest("Insumo", categoriaId, unidadId, presentacionId, null,
                10, null, BigDecimal.ONE, null, null, null, proveedorId);
        assertThatThrownBy(() -> insumoService.crear(request, "admin")).isInstanceOf(ApiException.class);
        assertThat(insumos.count()).isZero();
    }

    @Test
    void flujoEntradaSalidaActualizaStockYFiltraHistorial() throws Exception {
        InsumoResponse insumo = crearConStock(10);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(entradaJson(insumo.id(), 5, "L-01")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.responsable").value("Operario de prueba"));
        mvc.perform(post("/api/movimientos/salida").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(salidaJson(insumo.id(), loteId, 12)))
                .andExpect(status().isCreated());
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(3);
        assertThat(insumoService.obtener(insumo.id()).estado()).isEqualTo("Por Agotar");
        mvc.perform(get("/api/movimientos").with(user("logistica"))
                        .param("tipo", "SALIDA").param("insumoId", insumo.id().toString())
                        .param("desde", LocalDate.now().toString()).param("hasta", LocalDate.now().toString())
                        .param("texto", "FAR"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cantidad").value(12)).andExpect(jsonPath("$[0].fechaHora").exists());
    }

    @Test
    void salidaInsuficienteOCeroNoModificaStockNiHistorial() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        for (int cantidad : new int[]{6, 0, -2}) {
            mvc.perform(post("/api/movimientos").with(user("operario"))
                            .contentType(MediaType.APPLICATION_JSON).content(salidaJson(insumo.id(), loteId, cantidad)))
                    .andExpect(status().isBadRequest());
        }
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
        assertThat(insumoService.listarLotes(insumo.id()).getFirst().stockActual()).isEqualTo(5);
        assertThat(movimientoService.listar(null, null, null, insumo.id(), null)).hasSize(1);
    }

    @Test
    void loteAjenoYVencidoNoSeDespachan() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        var otro = insumoService.crear(new InsumoRequest("Otro", categoriaId, unidadId, presentacionId, null,
                0, null, BigDecimal.ONE, null, null, null, null), "admin");
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(salidaJson(otro.id(), loteId, 1)))
                .andExpect(status().isBadRequest());
        jdbc.update("update lotes set fecha_vencimiento = ? where id = ?", LocalDate.now().minusDays(1), loteId);
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(salidaJson(insumo.id(), loteId, 1)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/insumos/alertas/vencimientos").with(user("admin")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].estado").value("Vencido"));
    }

    @Test
    void entradaConFechaDiferenteOProveedorAusenteEsRechazada() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        String json = entradaJson(insumo.id(), 2, "L-01")
                .replace(LocalDate.now().plusMonths(6).toString(), LocalDate.now().plusMonths(7).toString());
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isConflict());
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(entradaJson(insumo.id(), 2, "L-02")
                                .replace("\"proveedorId\":" + proveedorId, "\"proveedorId\":null"))).andExpect(status().isBadRequest());
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
    }

    @Test
    void inactivacionConservaHistorialYBloqueaMovimientos() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        mvc.perform(delete("/api/insumos/{id}", insumo.id()).with(user("admin")))
                .andExpect(status().isNoContent());
        assertThat(movimientoService.listar(null, null, null, insumo.id(), null)).hasSize(1);
        mvc.perform(post("/api/movimientos").with(user("operario"))
                        .contentType(MediaType.APPLICATION_JSON).content(entradaJson(insumo.id(), 1, "L-01")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/insumos").with(user("admin"))).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void editarCatalogoNoCambiaStockNiAuditoria() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        mvc.perform(put("/api/insumos/{id}", insumo.id()).with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(20)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/insumos/{id}", insumo.id()).with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(insumoJson(5).replace("Paracetamol", "Nuevo nombre")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value(insumo.codigo()));
        assertThat(movimientoService.listar(null, null, null, insumo.id(), null).getFirst().codigoInsumo()).isEqualTo(insumo.codigo());
    }

    @Test
    void usuariosSeCreanConHashYLaBajaRevocaAcceso() throws Exception {
        MvcResult result = mvc.perform(post("/api/usuarios").with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombreCompleto":"Nuevo usuario","usuario":"nuevo","contrasena":"clave-prueba","rol":"OPERATIVO"}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.contrasena").doesNotExist()).andReturn();
        Usuario creado = usuarios.findByUsuario("nuevo").orElseThrow();
        assertThat(creado.getContrasena()).isNotEqualTo("clave-prueba");
        assertThat(encoder.matches("clave-prueba", creado.getContrasena())).isTrue();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        mvc.perform(delete("/api/usuarios/{id}", id).with(user("admin"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/insumos").with(user("nuevo"))).andExpect(status().isUnauthorized());
    }

    @Test
    void filtrosReportesYErroresDeFormato() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        mvc.perform(get("/api/insumos").with(user("admin")).param("nombre", "parac").param("categoria", "Medicamentos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/insumos/alertas/stock").with(user("admin")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(insumo.id()));
        mvc.perform(get("/api/reportes/movimientos.csv").with(user("logistica")))
                .andExpect(status().isOk()).andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"movimientos.csv\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Administrador de prueba")));
        mvc.perform(get("/api/movimientos").with(user("admin")).param("tipo", "INVALIDO"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/movimientos").with(user("admin"))
                        .param("desde", "2026-02-02").param("hasta", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dosSalidasSimultaneasNoSobregiranStock() throws Exception {
        InsumoResponse insumo = crearConStock(10);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> salida = () -> {
                inicio.await();
                try {
                    movimientoService.registrar(new MovimientoRequest(insumo.id(), TipoMovimiento.SALIDA, 8,
                            salidaId, null, null, null, null, null, null, loteId, null, clienteId), "operario");
                    return true;
                } catch (ApiException ex) {
                    assertThat(ex.getMessage()).contains("Stock insuficiente");
                    return false;
                }
            };
            Future<Boolean> a = executor.submit(salida);
            Future<Boolean> b = executor.submit(salida);
            inicio.countDown();
            assertThat(a.get(10, TimeUnit.SECONDS)).isNotEqualTo(b.get(10, TimeUnit.SECONDS));
        }
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(2);
        assertThat(insumoService.listarLotes(insumo.id()).getFirst().stockActual()).isEqualTo(2);
        assertThat(movimientoService.listar(TipoMovimiento.SALIDA, null, null, insumo.id(), null)).hasSize(1);
    }

    @Test
    void catalogosProvienenDeBdYSeValidanPorTipoYEstado() throws Exception {
        mvc.perform(get("/api/catalogos").with(user("operario")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.categorias[0].id").value(categoriaId))
                .andExpect(jsonPath("$.motivosSalida[?(@.clave == 'VENCIMIENTO')].permiteVencidos").value(org.hamcrest.Matchers.contains(true)));
        mvc.perform(post("/api/insumos").with(user("admin")).contentType(MediaType.APPLICATION_JSON)
                        .content(insumoJson(0).replace("\"categoriaId\":" + categoriaId, "\"categoriaId\":" + unidadId)))
                .andExpect(status().isBadRequest());
        jdbc.update("update catalogos set activo = false where id = ?", categoriaId);
        mvc.perform(post("/api/insumos").with(user("admin")).contentType(MediaType.APPLICATION_JSON)
                        .content(insumoJson(0))).andExpect(status().isBadRequest());
        assertThat(insumos.count()).isZero();
    }

    @Test
    void motivoDeEntradaNoSeAceptaEnSalida() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(salidaJson(insumo.id(), loteId, 1).replace("\"motivoId\":" + salidaId, "\"motivoId\":" + entradaId)))
                .andExpect(status().isBadRequest());
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
    }

    @Test
    void vencidoPuedeDarseDeBajaPeroNoDespacharseYMotivoHistoricoNoCambia() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        jdbc.update("update lotes set fecha_vencimiento = ? where id = ?", LocalDate.now().minusDays(1), loteId);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(salidaJson(insumo.id(), loteId, 2))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(salidaJson(insumo.id(), loteId, 2).replace("\"motivoId\":" + salidaId, "\"motivoId\":" + vencimientoId)
                                .replace("\"clienteId\":" + clienteId, "\"clienteId\":" + internoId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.motivo").value("Baja por vencimiento"));
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(3);
        jdbc.update("update catalogos set nombre = 'Nombre nuevo' where id = ?", vencimientoId);
        assertThat(movimientoService.listar(TipoMovimiento.SALIDA, null, null, insumo.id(), null).getFirst().motivo())
                .isEqualTo("Baja por vencimiento");
    }

    @Test
    void loteVigenteNoPuedeSalirPorVencimiento() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(salidaJson(insumo.id(), loteId, 1).replace("\"motivoId\":" + salidaId, "\"motivoId\":" + vencimientoId)
                                .replace("\"clienteId\":" + clienteId, "\"clienteId\":" + internoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generacionConcurrenteProduceCodigosDistintos() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<String> crear = () -> { inicio.await(); return crearConStock(0).codigo(); };
            Future<String> a = executor.submit(crear);
            Future<String> b = executor.submit(crear);
            inicio.countDown();
            assertThat(java.util.Set.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("FAR-000001", "FAR-000002");
        }
        assertThat(insumos.count()).isEqualTo(2);
    }

    @Test
    void generadorNoReutilizaUnCodigoExistente() {
        Insumo existente = insumos.save(new Insumo("FAR-000001", "Anterior", catalogos.findById(categoriaId).orElseThrow(),
                catalogos.findById(unidadId).orElseThrow(), catalogos.findById(presentacionId).orElseThrow(),
                null, 10, BigDecimal.ONE, null, null));
        assertThat(crearConStock(0).codigo()).isEqualTo("FAR-000002");
        assertThat(insumos.findById(existente.getId()).orElseThrow().getCodigo()).isEqualTo("FAR-000001");
    }

    @Test
    void almaceneroConsultaStockPeroNoMovimientosUsuariosReportesNiProveedores() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        for (String uri : new String[]{"/api/insumos", "/api/insumos/" + insumo.id(),
                "/api/insumos/" + insumo.id() + "/lotes", "/api/insumos/alertas/stock", "/api/catalogos", "/api/auth/perfil"}) {
            mvc.perform(get(uri).with(user("almacenero"))).andExpect(status().isOk());
        }
        for (String uri : new String[]{"/api/movimientos", "/api/usuarios", "/api/reportes/inventario.csv", "/api/proveedores", "/api/clientes"}) {
            mvc.perform(get(uri).with(user("almacenero"))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/insumos").with(user("almacenero")).contentType(MediaType.APPLICATION_JSON)
                .content(insumoJson(0))).andExpect(status().isForbidden());
        mvc.perform(post("/api/movimientos/entrada").with(user("almacenero")).contentType(MediaType.APPLICATION_JSON)
                .content(entradaJson(insumo.id(), 2, "L-01"))).andExpect(status().isForbidden());
        mvc.perform(put("/api/insumos/{id}", insumo.id()).with(user("almacenero")).contentType(MediaType.APPLICATION_JSON)
                .content(insumoJson(0))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/insumos/{id}", insumo.id()).with(user("almacenero"))).andExpect(status().isForbidden());
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
    }

    @Test
    void administradorPuedeCrearEmpleadoAlmaceneroYElEmpleadoNoPuedeCrearUsuarios() throws Exception {
        String json = """
                {"nombreCompleto":"Empleado de prueba","usuario":"empleado","contrasena":"clave-prueba","rol":"ALMACENERO"}
                """;
        mvc.perform(post("/api/usuarios").with(user("admin")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("ALMACENERO"));
        mvc.perform(get("/api/insumos").with(user("empleado"))).andExpect(status().isOk());
        mvc.perform(post("/api/usuarios").with(user("empleado")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void proveedorSeConsultaYSusDatosDeBdSeUsanEnLaEntradaSinConfiarEnTextosDelCliente() throws Exception {
        mvc.perform(get("/api/proveedores").with(user("operario")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].ruc").value("20000000001"))
                .andExpect(jsonPath("$[0].direccion").value("Dirección ficticia 101"));
        InsumoResponse insumo = crearConStock(5);
        assertThat(insumo.proveedorId()).isEqualTo(proveedorId);
        assertThat(insumo.proveedorDireccion()).isEqualTo("Dirección ficticia 101");
        String json = entradaJson(insumo.id(), 2, "L-01").replace("\"tipo\":\"ENTRADA\"",
                "\"tipo\":\"ENTRADA\",\"origenDestino\":\"Nombre manipulado\"");
        mvc.perform(post("/api/movimientos/entrada").with(user("operario")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.origenDestino").value("Proveedor de prueba"))
                .andExpect(jsonPath("$.proveedorRuc").value("20000000001"))
                .andExpect(jsonPath("$.proveedorDireccion").value("Dirección ficticia 101"));
        jdbc.update("update proveedores set nombre = 'Otro nombre', direccion = 'Otra dirección' where id = ?", proveedorId);
        var ultimo = movimientoService.listar(TipoMovimiento.ENTRADA, null, null, insumo.id(), null).getFirst();
        assertThat(ultimo.origenDestino()).isEqualTo("Proveedor de prueba");
        assertThat(ultimo.proveedorDireccion()).isEqualTo("Dirección ficticia 101");
    }

    @Test
    void proveedorInactivoOInexistenteNoPuedeUsarseParaIngresarStock() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        String json = entradaJson(insumo.id(), 2, "L-01");
        mvc.perform(post("/api/movimientos/entrada").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                .content(json.replace("\"proveedorId\":" + proveedorId, "\"proveedorId\":999999"))).andExpect(status().isBadRequest());
        jdbc.update("update proveedores set activo = false where id = ?", proveedorId);
        mvc.perform(get("/api/proveedores").with(user("operario"))).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/api/movimientos/entrada").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isBadRequest());
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
    }

    @Test
    void salidaTomaClienteDeBdYConservaSuDireccionHistorica() throws Exception {
        mvc.perform(get("/api/clientes").with(user("operario"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        String json = salidaJson(insumo.id(), loteId, 1).replace("\"tipo\":\"SALIDA\"",
                "\"tipo\":\"SALIDA\",\"origenDestino\":\"Destino manipulado\"");
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.origenDestino").value("Farmacia de prueba"))
                .andExpect(jsonPath("$.clienteId").value(clienteId)).andExpect(jsonPath("$.clienteRuc").value("20000000011"))
                .andExpect(jsonPath("$.clienteDireccion").value("Dirección ficticia 110"));
        jdbc.update("update clientes set nombre = 'Otro nombre', direccion = 'Otra dirección' where id = ?", clienteId);
        var movimiento = movimientoService.listar(TipoMovimiento.SALIDA, null, null, insumo.id(), null).getFirst();
        assertThat(movimiento.origenDestino()).isEqualTo("Farmacia de prueba");
        assertThat(movimiento.clienteDireccion()).isEqualTo("Dirección ficticia 110");
    }

    @Test
    void noAceptaDestinoLibreClienteInactivoONoExistente() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        String json = salidaJson(insumo.id(), loteId, 1);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("\"clienteId\":" + clienteId, "\"clienteId\":null,\"origenDestino\":\"Texto libre\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("\"clienteId\":" + clienteId, "\"clienteId\":999999"))).andExpect(status().isBadRequest());
        jdbc.update("update clientes set activo = false where id = ?", clienteId);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/clientes").with(user("operario"))).andExpect(jsonPath("$.length()").value(1));
        assertThat(insumoService.obtener(insumo.id()).stockActual()).isEqualTo(5);
    }

    @Test
    void bajaRequiereDestinoInternoYDespachoRequiereCliente() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        String json = salidaJson(insumo.id(), loteId, 1);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("\"clienteId\":" + clienteId, "\"clienteId\":" + internoId)))
                .andExpect(status().isBadRequest());
        String merma = json.replace("\"motivoId\":" + salidaId, "\"motivoId\":" + mermaId);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(merma)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(merma.replace("\"clienteId\":" + clienteId, "\"clienteId\":" + internoId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.origenDestino").value("Área de bajas"));
    }

    @Test
    void devolucionVaAProveedorNoACliente() throws Exception {
        InsumoResponse insumo = crearConStock(5);
        Long loteId = insumoService.listarLotes(insumo.id()).getFirst().id();
        String json = salidaJson(insumo.id(), loteId, 1).replace("\"motivoId\":" + salidaId, "\"motivoId\":" + devolucionId);
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/movimientos/salida").with(user("operario")).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("\"clienteId\":" + clienteId, "\"clienteId\":null,\"proveedorId\":" + proveedorId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.origenDestino").value("Proveedor de prueba"))
                .andExpect(jsonPath("$.proveedorRuc").value("20000000001")).andExpect(jsonPath("$.clienteId").isEmpty());
    }

    private InsumoResponse crearConStock(int stock) {
        return insumoService.crear(new InsumoRequest("Paracetamol", categoriaId, unidadId,
                presentacionId, "500 mg", stock, 10, new BigDecimal("12.50"), "A-1",
                "L-01", LocalDate.now().plusMonths(6), proveedorId), "admin");
    }
    private RequestPostProcessor user(String nombre) {
        String token = jwtService.emitir(UsuarioResponse.from(usuarios.findByUsuario(nombre).orElseThrow())).token();
        return request -> { request.addHeader("Authorization", "Bearer " + token); return request; };
    }
    private Long opcion(TipoCatalogo tipo, String clave, String nombre, boolean permiteVencidos) {
        return catalogos.save(new Catalogo(tipo, clave, nombre, permiteVencidos)).getId();
    }
    private String insumoJson(int stock) {
        return """
                {"nombre":"Paracetamol","categoriaId":%d,"unidadMedidaId":%d,"presentacionId":%d,"stockActual":%d,"precioUnitario":12.50}
                """.formatted(categoriaId, unidadId, presentacionId, stock);
    }
    private String entradaJson(Long insumoId, int cantidad, String lote) {
        return """
                {"insumoId":%d,"tipo":"ENTRADA","cantidad":%d,"motivoId":%d,"proveedorId":%d,
                 "numeroGuia":"F-001","numeroLote":"%s","fechaVencimiento":"%s"}
                """.formatted(insumoId, cantidad, entradaId, proveedorId, lote, LocalDate.now().plusMonths(6));
    }
    private String salidaJson(Long insumoId, Long loteId, int cantidad) {
        return """
                {"insumoId":%d,"tipo":"SALIDA","cantidad":%d,"motivoId":%d,"clienteId":%d,"loteId":%d}
                """.formatted(insumoId, cantidad, salidaId, clienteId, loteId);
    }
}
