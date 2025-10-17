package com.geriatriccare.unit.entity;

import com.geriatriccare.entity.MedicationForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicationForm Enum Tests")
class MedicationFormTest {

    @Test
    @DisplayName("Should have TABLET form")
    void shouldHaveTabletForm() {
        MedicationForm form = MedicationForm.TABLET;
        assertThat(form).isNotNull();
        assertThat(form.name()).isEqualTo("TABLET");
    }

    @Test
    @DisplayName("Should have CAPSULE form")
    void shouldHaveCapsuleForm() {
        MedicationForm form = MedicationForm.CAPSULE;
        assertThat(form).isNotNull();
        assertThat(form.name()).isEqualTo("CAPSULE");
    }

    @Test
    @DisplayName("Should have LIQUID form")
    void shouldHaveLiquidForm() {
        MedicationForm form = MedicationForm.LIQUID;
        assertThat(form).isNotNull();
        assertThat(form.name()).isEqualTo("LIQUID");
    }

    @Test
    @DisplayName("Should have INJECTION form")
    void shouldHaveInjectionForm() {
        MedicationForm form = MedicationForm.INJECTION;
        assertThat(form).isNotNull();
        assertThat(form.name()).isEqualTo("INJECTION");
    }

    @Test
    @DisplayName("Should have TOPICAL form")
    void shouldHaveTopicalForm() {
        MedicationForm form = MedicationForm.TOPICAL;
        assertThat(form).isNotNull();
        assertThat(form.name()).isEqualTo("TOPICAL");
    }

    @Test
    @DisplayName("Should have display name for TABLET")
    void shouldHaveDisplayNameForTablet() {
        assertThat(MedicationForm.TABLET.getDisplayName()).isEqualTo("Tablet");
    }

    @Test
    @DisplayName("Should have display name for CAPSULE")
    void shouldHaveDisplayNameForCapsule() {
        assertThat(MedicationForm.CAPSULE.getDisplayName()).isEqualTo("Capsule");
    }

    @Test
    @DisplayName("Should have display name for LIQUID")
    void shouldHaveDisplayNameForLiquid() {
        assertThat(MedicationForm.LIQUID.getDisplayName()).isEqualTo("Liquid");
    }

    @Test
    @DisplayName("Should have display name for INJECTION")
    void shouldHaveDisplayNameForInjection() {
        assertThat(MedicationForm.INJECTION.getDisplayName()).isEqualTo("Injection");
    }

    @Test
    @DisplayName("Should have display name for TOPICAL")
    void shouldHaveDisplayNameForTopical() {
        assertThat(MedicationForm.TOPICAL.getDisplayName()).isEqualTo("Topical");
    }
}