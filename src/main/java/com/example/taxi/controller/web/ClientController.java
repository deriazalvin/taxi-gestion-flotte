package com.example.taxi.controller.web;

import com.example.taxi.dto.request.ClientRequest;
import com.example.taxi.dto.response.ClientResponse;
import com.example.taxi.service.ClientService;
import com.example.taxi.util.WebSortSupport;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "name,asc") String sort,
                       Model model) {
        model.addAttribute("page", clientService.search(keyword,
                PageRequest.of(page, 10, WebSortSupport.toSort(sort, "name", true,
                        "name", "phone", "email", "createdAt"))));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "clients/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String form(Model model) {
        model.addAttribute("client", new ClientForm());
        return "clients/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String edit(@PathVariable Long id, Model model) {
        ClientResponse c = clientService.getById(id);
        model.addAttribute("client", new ClientForm(c));
        return "clients/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public String save(@RequestParam(required = false) Long id,
                       @ModelAttribute("client") @Valid ClientForm form,
                       BindingResult bindingResult, Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "clients/form";
        }
        ClientRequest req = form.toRequest();
        if (id == null) {
            var created = clientService.create(req);
            ra.addFlashAttribute("success", "Client « " + created.name() + " » créé.");
        } else {
            var updated = clientService.update(id, req);
            ra.addFlashAttribute("success", "Client « " + updated.name() + " » mis à jour.");
        }
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        clientService.delete(id);
        ra.addFlashAttribute("success", "Client supprimé.");
        return "redirect:/clients";
    }

    public record ClientForm(Long id, String name, String phone, String email, String address) {

        public ClientForm() {
            this(null, "", "", "", "");
        }

        public ClientForm(ClientResponse c) {
            this(c.id(), c.name(), c.phone(), c.email(), c.address());
        }

        public ClientRequest toRequest() {
            return new ClientRequest(name, phone,
                    email != null && email.isBlank() ? null : email,
                    address != null && address.isBlank() ? null : address);
        }
    }
}