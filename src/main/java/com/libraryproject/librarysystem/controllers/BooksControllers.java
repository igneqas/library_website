package com.libraryproject.librarysystem.controllers;

import com.libraryproject.librarysystem.domain.*;
import com.libraryproject.librarysystem.repositories.AuthorsRepository;
import com.libraryproject.librarysystem.repositories.BooksRepository;
import com.libraryproject.librarysystem.repositories.UsersRepository;
import com.libraryproject.librarysystem.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
public class BooksControllers {

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthorsRepository authorsRepository;

    @GetMapping("/addnewbook")
    public String bookList(Model model) {

        model.addAttribute("authors", authorsRepository.findAll());

        return "addnewbook.html";
    }

    @GetMapping("/addthisnewbook")
    public String addBook(@RequestParam(value = "title") String title,
                          @RequestParam(value = "url") String url,
                          @RequestParam(value = "author") String authorID) {
        Books book = new Books(title, url);
        Optional<Authors> authorOP = authorsRepository.findById(Integer.parseInt(authorID.trim()));
        Authors author = authorOP.get();
        List<Books> listForAuthor = new ArrayList<>();
        listForAuthor.add(book);
        author.setBooksList(listForAuthor);
        List<Authors> authorsList = new ArrayList<>();
        authorsList.add(author);
        book.setAuthorsList(authorsList);
        book.setAvailability(Availability.AVAILABLE);
        booksRepository.save(book);
        return "redirect:/bookslist";
    }

    @GetMapping("/viewbook/{id}")
    public String viewOneBook(Model model, @PathVariable int id) {
        Books book = booksRepository.getById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails currentUser = (MyUserDetails) authentication.getPrincipal();
        Users user = usersRepository.getById(currentUser.getUserID());

        if (user.getAccessLevel() == AccessLevel.LIBRARIAN) {
            System.out.println("It's librarian " + currentUser);
            model.addAttribute("level","librarian");
        } else {
            System.out.println("It's user " + currentUser);
            model.addAttribute("level","user");
        }


        model.addAttribute("book", book);
        return "infoonebook.html";
    }

    @GetMapping("/viewbook/delete/{id}")
    public String deleteBook(@PathVariable int id) {
        System.out.println("Trying to delete this book: " + id );
        booksRepository.deleteById(id);

        return "redirect:/bookslist";
    }

    @GetMapping("/viewbook/edit/{id}")
    public String editBook(Model model,@PathVariable int id) {
//        We don't need to create new book, but rather
//        get the existing book from database, so the html displays its actual info in the text fields
//        Books book = new Books();
        Books book = booksRepository.getById(id);

        System.out.println(book.getId() + " | " + book.getAvailability() + " | " + book.getTitle() + " | " + book.getUrl());
        model.addAttribute("book", book);
        return "editbook.html";
    }

    /**
     * Few changes required, I'm leaving this method commented so you can compare with new one below
      */
//    @GetMapping("/editthisbook")
//    public String editThisBook(Model model, @PathVariable int id, @RequestParam String title, String url) {
//        Books book = booksRepository.getById(id);
//        book.setTitle(title);
//        book.setUrl(url);
//        booksRepository.save(book);
//        book = booksRepository.getById(id);
//        model.addAttribute("bookb", book);
//        return "infoonebook.html";
//    }

    // As written in the html code, it's needed post mapping, to receive the whole java object
    @PostMapping("/editthisbook")
    public String editThisBook(@Valid Books book, Model model) {
        booksRepository.save(book);
        System.out.println("This is the id of the book from html: " + book.getId());
        book = booksRepository.getById(book.getId());
        model.addAttribute("book", book);
        return "redirect:/bookslist";
    }

}
