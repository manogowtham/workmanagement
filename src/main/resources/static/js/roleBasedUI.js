// roleBasedUI.js

document.addEventListener("DOMContentLoaded", function () {
    const isSuperAdmin = document.body.dataset.role === "SUPERADMIN";
    const isAdmin = document.body.dataset.role === "ADMIN";
  
    // Hide engineer form link for admin
    if (!isSuperAdmin) {
      const engineerLink = document.querySelector('a[href="/engineer-form"]');
      if (engineerLink) {
        engineerLink.style.display = "none";
      }
    }
  
    // Disable actions for ADMIN
    if (isAdmin) {
      document.querySelectorAll(".action-edit, .action-delete").forEach(el => {
        el.style.display = "none";
      });
    }
  
    // If neither role, disable nav
    if (!isSuperAdmin && !isAdmin) {
      document.querySelectorAll("nav a").forEach(el => {
        el.classList.add("disabled");
      });
    }
  });
  