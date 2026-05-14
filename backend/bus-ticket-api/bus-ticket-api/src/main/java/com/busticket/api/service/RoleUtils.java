package com.busticket.api.service;

import java.text.Normalizer;
import java.util.Locale;

final class RoleUtils {

  private RoleUtils() {
  }

  static boolean isStaffRole(String role) {
    String normalized = normalizeRole(role);
    return "nhanvien".equals(normalized)
            || "staff".equals(normalized);
  }

  private static String normalizeRole(String role) {
    if (role == null) {
      return "";
    }

    String withoutAccents = Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");

    return withoutAccents
            .replaceAll("[^A-Za-z0-9]", "")
            .toLowerCase(Locale.ROOT);
  }
}
